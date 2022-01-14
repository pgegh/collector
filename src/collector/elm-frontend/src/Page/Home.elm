module Page.Home exposing (Model, Msg, init, isChangeDB, update, view)

import DB exposing (DB)
import Entry exposing (Entry)
import FileName
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
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


type EntryData
    = Audio
        { id : String
        , name : String
        , singer : String
        , year : Int
        , language : String
        , country : String
        , genre : String
        }
    | Book
        { id : String
        , name : String
        , author : String
        , year : Int
        , language : String
        , country : String
        , category : String
        }
    | Game
        { id : String
        , name : String
        , year : Int
        , company : String
        , platform : String
        , country : String
        , genre : String
        }
    | Video
        { id : String
        , name : String
        , originalTitle : String
        , year : Int
        , language : String
        , country : String
        , genre : String
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


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UpdateSelectedCategory category ->
            ( { model | selectedCategory = category }, Cmd.none )

        UpdateSelectedEntry entry ->
            ( { model | selectedEntry = Just entry }, Cmd.none )

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
                                Audio
                                    { id = ""
                                    , name = ""
                                    , singer = ""
                                    , year = 0
                                    , language = ""
                                    , country = ""
                                    , genre = ""
                                    }

                            "Book" ->
                                Book
                                    { id = ""
                                    , name = ""
                                    , author = ""
                                    , year = 0
                                    , language = ""
                                    , country = ""
                                    , category = ""
                                    }

                            "Game" ->
                                Game
                                    { id = ""
                                    , name = ""
                                    , year = 0
                                    , company = ""
                                    , platform = ""
                                    , country = ""
                                    , genre = ""
                                    }

                            _ ->
                                Video
                                    { id = ""
                                    , name = ""
                                    , originalTitle = ""
                                    , year = 0
                                    , language = ""
                                    , country = ""
                                    , genre = ""
                                    }
                        )
              }
            , Cmd.none
            )



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
