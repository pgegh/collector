module Page.Home exposing (Model, Msg, init, isChangeDB, update, view)

import DB exposing (DB)
import Entry exposing (Entry)
import FileName
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


type EntryData
    = Audio
        { name : String
        , artists : List String
        , year : Int
        , languages : List String
        , genre : String
        }
    | Book
        { name : String
        , authors : List String
        , publisher : String
        , year : Int
        , language : String
        , categories : List String
        }
    | Game
        { name : String
        , year : Int
        , companies : List String
        , platform : String
        , genres : List String
        }
    | Video
        { name : String
        , originalTitle : String
        , year : Int
        , languages : List String
        , countries : List String
        , genres : List String
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
                                Audio
                                    { name = ""
                                    , artists = []
                                    , year = 0
                                    , languages = []
                                    , genre = ""
                                    }

                            "Book" ->
                                Book
                                    { name = ""
                                    , authors = []
                                    , publisher = ""
                                    , year = 0
                                    , language = ""
                                    , categories = []
                                    }

                            "Game" ->
                                Game
                                    { name = ""
                                    , year = 0
                                    , companies = []
                                    , platform = ""
                                    , genres = []
                                    }

                            _ ->
                                Video
                                    { name = ""
                                    , originalTitle = ""
                                    , year = 0
                                    , languages = []
                                    , countries = []
                                    , genres = []
                                    }
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
                        audioDecoder

                    "Book" ->
                        bookDecoder

                    "Game" ->
                        gameDecoder

                    _ ->
                        videoDecoder
                )
        }



-- Json


initAudio : String -> List String -> Int -> List String -> String -> EntryData
initAudio name artists year languages genre =
    Audio
        { name = name
        , artists = artists
        , year = year
        , languages = languages
        , genre = genre
        }


audioDecoder : JD.Decoder EntryData
audioDecoder =
    JD.map5 initAudio
        (JD.field "name" JD.string)
        (JD.field "artists" (JD.list JD.string))
        (JD.field "year" JD.int)
        (JD.field "languages" (JD.list JD.string))
        (JD.field "genre" JD.string)


initBook : String -> List String -> String -> Int -> String -> List String -> EntryData
initBook name authors publisher year language categories =
    Book
        { name = name
        , authors = authors
        , publisher = publisher
        , year = year
        , language = language
        , categories = categories
        }


bookDecoder : JD.Decoder EntryData
bookDecoder =
    JD.map6 initBook
        (JD.field "name" JD.string)
        (JD.field "authors" (JD.list JD.string))
        (JD.field "publisher" JD.string)
        (JD.field "year" JD.int)
        (JD.field "language" JD.string)
        (JD.field "categories" (JD.list JD.string))


initGame : String -> Int -> List String -> String -> List String -> EntryData
initGame name year companies platform genres =
    Game
        { name = name
        , year = year
        , companies = companies
        , platform = platform
        , genres = genres
        }


gameDecoder : JD.Decoder EntryData
gameDecoder =
    JD.map5 initGame
        (JD.field "name" JD.string)
        (JD.field "year" JD.int)
        (JD.field "companies" (JD.list JD.string))
        (JD.field "platform" JD.string)
        (JD.field "genres" (JD.list JD.string))


initVideo : String -> String -> Int -> List String -> List String -> List String -> EntryData
initVideo name originalTitle year languages countries genres =
    Video
        { name = name
        , originalTitle = originalTitle
        , year = year
        , languages = languages
        , countries = countries
        , genres = genres
        }


videoDecoder : JD.Decoder EntryData
videoDecoder =
    JD.map6 initVideo
        (JD.field "name" JD.string)
        (JD.field "original-title" JD.string)
        (JD.field "year" JD.int)
        (JD.field "languages" (JD.list JD.string))
        (JD.field "countries" (JD.list JD.string))
        (JD.field "genres" (JD.list JD.string))
