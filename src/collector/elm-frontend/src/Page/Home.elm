module Page.Home exposing (Model, Msg, init, isChangeDB, update, view)

import DataStructures.DB exposing (DB)
import DataStructures.Entry exposing (Entry)
import DataStructures.EntryData as EntryData exposing (EntryData)
import DataStructures.FileName as FileName
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import HttpSettings exposing (baseUrl)
import Json.Decode as JD



-- MODEL


type alias Model =
    { db : DB
    , selectedEntry : Maybe Entry
    , selectedEntryData : Maybe EntryData
    , selectedCategory : String
    , changeDB : Bool
    , state : HomeState
    , newEntryData : Maybe EntryData
    }


type HomeState
    = Deleting
    | Editing
    | Adding
    | Viewing


init : DB -> Model
init db =
    { db = db
    , selectedEntry = Nothing
    , selectedEntryData = Nothing
    , selectedCategory = "All"
    , changeDB = False
    , state = Viewing
    , newEntryData = Nothing
    }


isChangeDB : Model -> Bool
isChangeDB model =
    model.changeDB



-- UPDATE


type Msg
    = UpdateSelectedCategory String
    | UpdateSelectedEntry Entry
    | ChangeDB
    | UpdateState HomeState
    | UpdateNewEntryCategory String
    | GotEntryData (Result Http.Error EntryData)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UpdateSelectedCategory category ->
            ( { model | selectedCategory = category }, Cmd.none )

        UpdateSelectedEntry entry ->
            ( { model | selectedEntry = Just entry }, getSelectedEntryData entry )

        ChangeDB ->
            ( { model | changeDB = True }, Cmd.none )

        UpdateState state ->
            ( { model | state = state }, Cmd.none )

        UpdateNewEntryCategory category ->
            ( { model
                | newEntryData =
                    Just
                        (case category of
                            "Audio" ->
                                EntryData.initAudio

                            "Book" ->
                                EntryData.initBook

                            "Game" ->
                                EntryData.initGame

                            _ ->
                                EntryData.initVideo
                        )
              }
            , Cmd.none
            )

        GotEntryData result ->
            case result of
                Err _ ->
                    ( model, Cmd.none )

                Ok entryData ->
                    ( { model | selectedEntryData = Just entryData }, Cmd.none )



-- VIEW


view : Model -> Html Msg
view model =
    div []
        [ div [ id "navigation" ]
            [ select
                [ name "Categories"
                , on "change" (JD.map UpdateSelectedCategory targetValue)
                ]
                (option [ value "All" ] [ text "All" ]
                    :: List.map
                        (\category ->
                            option [ value category ] [ text category ]
                        )
                        model.db.categories
                )
            , span []
                [ label [] [ text <| FileName.getString model.db.fileName ]
                , label [] [ text <| model.db.dbCreatedDate ++ " - " ++ model.db.dbUpdatedDate ]
                ]
            , button [ onClick ChangeDB ] [ text "Change Database" ]
            ]
        , div [ id "entries" ]
            [ h1 [] [ text model.selectedCategory ]
            , div
                []
                (List.map
                    (entryToLabel model.selectedEntry)
                    model.db.entries
                )
            ]
        , div [ id "details" ]
            [ h1 [] [ text "Details" ]
            , div []
                [ button
                    [ hidden (model.state /= Deleting)
                    , onClick (UpdateState Viewing)
                    ]
                    [ text "Confirm Delete" ]
                , button
                    [ hidden (model.state == Viewing || model.state == Deleting)
                    ]
                    [ text "Apply" ]
                , button
                    [ hidden (model.state /= Viewing)
                    , onClick (UpdateState Editing)
                    ]
                    [ text "Edit" ]
                , button
                    [ hidden (model.state /= Viewing)
                    , onClick (UpdateState Adding)
                    ]
                    [ text "Add New" ]
                , button
                    [ hidden (model.state == Viewing)
                    , onClick (UpdateState Viewing)
                    ]
                    [ text "Cancel" ]
                , button
                    [ hidden (model.state /= Viewing)
                    , onClick (UpdateState Deleting)
                    ]
                    [ text "Delete" ]
                ]
            , div [ hidden (model.state /= Adding && model.state /= Editing) ]
                [ select
                    [ name "New Entry Category"
                    , on "change" (JD.map UpdateNewEntryCategory targetValue)
                    ]
                    [ option [ value "Audio" ] [ text "Audio" ]
                    , option [ value "Book" ] [ text "Book" ]
                    , option [ value "Game" ] [ text "Game" ]
                    , option [ value "Video" ] [ text "Video" ]
                    ]
                ]
            ]
        ]


entryToLabel : Maybe Entry -> Entry -> Html Msg
entryToLabel selectedEntry entry =
    div []
        [ label
            [ onClick (UpdateSelectedEntry entry)
            , class
                (if selectedEntry == Just entry then
                    "Selected"

                 else
                    "NotSelected"
                )
            ]
            [ text entry.name ]
        ]



-- Http


getSelectedEntryData : Entry -> Cmd Msg
getSelectedEntryData entry =
    Http.get
        { url = baseUrl ++ "/get-entry?" ++ entry.id
        , expect =
            Http.expectJson GotEntryData
                (case entry.category of
                    "Audio" ->
                        EntryData.audioDecoder

                    "Book" ->
                        EntryData.bookDecoder

                    "Game" ->
                        EntryData.gameDecoder

                    _ ->
                        EntryData.videoDecoder
                )
        }
