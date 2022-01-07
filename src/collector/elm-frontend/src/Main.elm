module Main exposing (main)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode as JD


main : Program () Model Msg
main =
    Browser.document
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }



-- MODEL


type Model
    = Loading
    | StartPage StartPageData
    | ErrorDBFileNames
    | ErrorPage String


type alias StartPageData =
    { dbFileNames : FileNames
    , selectedDBFile : String
    , newDBFileName : String
    }


type alias FileNames =
    List String


init : () -> ( Model, Cmd Msg )
init _ =
    ( Loading, loadFileNames )


isDBFileSelected : StartPageData -> Bool
isDBFileSelected spData =
    case spData.selectedDBFile of
        "" ->
            False

        _ ->
            True



-- UPDATE


type Msg
    = Load
    | Create
    | GetFileNames
    | GotFileNames (Result Http.Error FileNames)
    | UpdateSelectedDBFileName String
    | UpdateNewDBFileName String


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Load ->
            ( model, Cmd.none )

        Create ->
            ( model, Cmd.none )

        GetFileNames ->
            ( Loading, loadFileNames )

        GotFileNames result ->
            case result of
                Err _ ->
                    ( ErrorDBFileNames, Cmd.none )

                Ok dbFileNames ->
                    ( StartPage
                        { dbFileNames = dbFileNames
                        , selectedDBFile = ""
                        , newDBFileName = ""
                        }
                    , Cmd.none
                    )

        UpdateSelectedDBFileName fileName ->
            case model of
                StartPage startPageData ->
                    ( StartPage { startPageData | selectedDBFile = fileName }, Cmd.none )

                _ ->
                    ( ErrorPage "Trying to update selected database file name when not in StartPage.", Cmd.none )

        UpdateNewDBFileName fileName ->
            case model of
                StartPage startPageData ->
                    ( StartPage { startPageData | newDBFileName = fileName }, Cmd.none )

                _ ->
                    ( ErrorPage "Trying to change new database file name when not in StartPage.", Cmd.none )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none



-- VIEW


view : Model -> Browser.Document Msg
view model =
    case model of
        Loading ->
            { title = "Collector - Loading"
            , body = [ text "Loading..." ]
            }

        StartPage startPageData ->
            { title = "Collector - Set Database"
            , body =
                [ h1 [] [ text "COLLECTOR" ]
                , h2 [] [ text "Select a database" ]
                , div []
                    [ select
                        [ name "Available databases"
                        , size 10
                        , on "change" (JD.map UpdateSelectedDBFileName targetValue)
                        ]
                        (List.map
                            (\fileName ->
                                option [ value fileName ] [ text fileName ]
                            )
                            startPageData.dbFileNames
                        )
                    , button
                        [ onClick Load
                        , disabled (not (isDBFileSelected startPageData))
                        ]
                        [ text "Load Selected Database" ]
                    ]
                , div []
                    [ input [ type_ "text", on "change" (JD.map UpdateNewDBFileName targetValue) ] []
                    , button [ onClick Create ] [ text "Create New Database" ]
                    ]
                ]
            }

        ErrorDBFileNames ->
            { title = "Collector - Error Getting Database files"
            , body =
                [ h1 [] [ text "Error! Could not retrieve available database files" ]
                , button [ onClick GetFileNames ] [ text "Retry" ]
                ]
            }

        ErrorPage error ->
            { title = "Collector - Error"
            , body =
                [ h1 [] [ text "Error!" ]
                , p [] [ text error ]
                ]
            }



-- HTTP


baseUrl : String
baseUrl =
    "http://localhost:8001"


loadFileNames : Cmd Msg
loadFileNames =
    Http.get
        { url = baseUrl ++ "/get-available-database-files"
        , expect = Http.expectJson GotFileNames fileNameDecoder
        }



-- JSON


fileNameDecoder : JD.Decoder (List String)
fileNameDecoder =
    JD.list JD.string
