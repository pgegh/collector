-- Copyright © 2021 Hovig Manjikian
--
-- This file is part of collector.
--
-- collector is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- collector is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with json.  If not, see <https://www.gnu.org/licenses/>.


module Main exposing (main)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode as JD
import Json.Encode as JE


main : Program () Model Msg
main =
    Browser.document
        { init = init
        , subscriptions = subscriptions
        , update = update
        , view = view
        }



-- MODEL


type Model
    = ErrorDBFileNames
    | ErrorPage String
    | Loading
    | MainPage MainPageData
    | StartPage StartPageData


type alias StartPageData =
    { dbFileNames : FileNames
    , newDBFileName : String
    , selectedDBFile : String
    }


type alias MainPageReceivedData =
    { category : String
    , dbCreatedDate : String
    , dbFileName : String
    , dbModifiedDate : String
    , entries : List Entry
    , categories : List String
    }


type alias MainPageLocalData =
    { state : MainPageState
    , newID : String
    , newName : String
    , newCategory : String
    }


type alias MainPageData =
    { local : MainPageLocalData
    , received : MainPageReceivedData
    }


type MainPageState
    = View
    | Edit
    | Add
    | Delete


type alias Entry =
    { category : String
    , id : String
    , name : String
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
    = CreateNewDB
    | GetFileNames
    | GotFileNames (Result Http.Error FileNames)
    | GotMainPageData (Result Http.Error MainPageReceivedData)
    | LoadDB
    | UpdateNewDBFileName String
    | UpdateSelectedCategory String
    | UpdateSelectedDBFileName String
    | UpdateMainPageState MainPageState
    | UpdateNewID String
    | UpdateNewName String
    | UpdateNewCategory String
    | ApplyEntry


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        CreateNewDB ->
            let
                fileName =
                    case model of
                        StartPage spData ->
                            spData.newDBFileName

                        _ ->
                            ""
            in
            ( Loading, loadDB fileName )

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

        GotMainPageData result ->
            case result of
                Err _ ->
                    ( ErrorPage "Error in the loaded database!", Cmd.none )

                Ok mainPageReceivedData ->
                    ( MainPage
                        { local =
                            { state = View
                            , newID = ""
                            , newName = ""
                            , newCategory = ""
                            }
                        , received = mainPageReceivedData
                        }
                    , Cmd.none
                    )

        LoadDB ->
            let
                fileName =
                    case model of
                        StartPage spData ->
                            spData.selectedDBFile

                        _ ->
                            ""
            in
            ( Loading, loadDB fileName )

        UpdateNewDBFileName fileName ->
            case model of
                StartPage startPageData ->
                    ( StartPage { startPageData | newDBFileName = fileName }, Cmd.none )

                _ ->
                    ( ErrorPage "Trying to change new database file name when not in StartPage.", Cmd.none )

        UpdateSelectedCategory category ->
            case model of
                MainPage mainPageData ->
                    let
                        r =
                            mainPageData.received

                        l =
                            mainPageData.local
                    in
                    ( MainPage
                        { local = l
                        , received = { r | category = category }
                        }
                    , Cmd.none
                    )

                _ ->
                    ( ErrorPage "Trying to change category when not in MainPage.", Cmd.none )

        UpdateSelectedDBFileName fileName ->
            case model of
                StartPage startPageData ->
                    ( StartPage { startPageData | selectedDBFile = fileName }, Cmd.none )

                _ ->
                    ( ErrorPage "Trying to update selected database file name when not in StartPage.", Cmd.none )

        UpdateMainPageState state ->
            case model of
                MainPage mainPageData ->
                    let
                        r =
                            mainPageData.received

                        l =
                            mainPageData.local
                    in
                    ( MainPage
                        { local = { l | state = state }
                        , received = r
                        }
                    , Cmd.none
                    )

                _ ->
                    ( ErrorPage "Trying to update main page state when not in MainPage.", Cmd.none )

        UpdateNewID id ->
            case model of
                MainPage mainPageData ->
                    let
                        r =
                            mainPageData.received

                        l =
                            mainPageData.local
                    in
                    ( MainPage
                        { local = { l | newID = id }
                        , received = r
                        }
                    , Cmd.none
                    )

                _ ->
                    ( ErrorPage "Trying to update newID when not in MainPage.", Cmd.none )

        UpdateNewName name ->
            case model of
                MainPage mainPageData ->
                    let
                        r =
                            mainPageData.received

                        l =
                            mainPageData.local
                    in
                    ( MainPage
                        { local = { l | newName = name }
                        , received = r
                        }
                    , Cmd.none
                    )

                _ ->
                    ( ErrorPage "Trying to update newName when not in MainPage.", Cmd.none )

        UpdateNewCategory category ->
            case model of
                MainPage mainPageData ->
                    let
                        r =
                            mainPageData.received

                        l =
                            mainPageData.local
                    in
                    ( MainPage
                        { local = { l | newCategory = category }
                        , received = r
                        }
                    , Cmd.none
                    )

                _ ->
                    ( ErrorPage "Trying to update newCategory when not in MainPage.", Cmd.none )

        ApplyEntry ->
            case model of
                MainPage mainPageData ->
                    let
                        r =
                            mainPageData.received

                        l =
                            mainPageData.local
                    in
                    ( MainPage mainPageData
                    , case l.newCategory of
                        "Audio" ->
                            Cmd.none

                        "Book" ->
                            Cmd.none

                        "Game" ->
                            Cmd.none

                        "Video" ->
                            addVideo l.newID l.newName

                        _ ->
                            Cmd.none
                    )

                _ ->
                    ( ErrorPage "Trying to apply an add/edit action when not in MainPage.", Cmd.none )



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
                , div []
                    [ h2 [] [ text "Select a database" ]
                    , select
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
                        [ onClick GetFileNames ]
                        [ text "Refresh" ]
                    , button
                        [ onClick LoadDB
                        , disabled (not (isDBFileSelected startPageData))
                        ]
                        [ text "Load Selected Database" ]
                    ]
                , div []
                    [ h2 [] [ text "Create a new database" ]
                    , input [ type_ "text", on "change" (JD.map UpdateNewDBFileName targetValue) ] []
                    , button [ onClick CreateNewDB, disabled (startPageData.newDBFileName == "") ] [ text "Create" ]
                    ]
                , div []
                    [ p [] [ text "To delete an existing database, delete the file from the file-system." ] ]
                ]
            }

        ErrorDBFileNames ->
            { title = "Collector - Error Getting Database files"
            , body =
                [ h1 [] [ text "Error! Could not retrieve available database files" ]
                , button [ onClick GetFileNames ] [ text "Retry" ]
                ]
            }

        MainPage mainPageData ->
            { title = "Collector - Main Page"
            , body =
                [ div [ id "navigation" ]
                    [ select
                        [ name "Categories"
                        , on "change" (JD.map UpdateSelectedCategory targetValue)
                        ]
                        ([ option [ value "All" ] [ text "All" ]
                         ]
                            ++ List.map
                                (\category ->
                                    option [ value category ] [ text category ]
                                )
                                mainPageData.received.categories
                        )
                    , span []
                        [ label [] [ text mainPageData.received.dbFileName ]
                        , label [] [ text <| mainPageData.received.dbCreatedDate ++ " - " ++ mainPageData.received.dbModifiedDate ]
                        ]
                    , button [ onClick GetFileNames ] [ text "Start Page" ]
                    ]
                , div [ id "entries" ]
                    [ h1 [] [ text mainPageData.received.category ]
                    , select
                        [ name "Entries"
                        , size 20
                        ]
                        (List.map
                            (\entry ->
                                option [ value entry.id ] [ text entry.name ]
                            )
                            mainPageData.received.entries
                        )
                    ]
                , div [ id "details" ]
                    [ h1 [] [ text "Details" ]
                    , div []
                        [ button
                            [ hidden (mainPageData.local.state /= Delete)
                            , onClick (UpdateMainPageState View)
                            ]
                            [ text "Confirm Delete" ]
                        , button
                            [ hidden (mainPageData.local.state == View || mainPageData.local.state == Delete)
                            , onClick ApplyEntry
                            ]
                            [ text "Apply" ]
                        , button
                            [ hidden (mainPageData.local.state /= View)
                            , onClick (UpdateMainPageState Edit)
                            ]
                            [ text "Edit" ]
                        , button
                            [ hidden (mainPageData.local.state /= View)
                            , onClick (UpdateMainPageState Add)
                            ]
                            [ text "Add New" ]
                        , button
                            [ hidden (mainPageData.local.state == View)
                            , onClick (UpdateMainPageState View)
                            ]
                            [ text "Cancel" ]
                        , button
                            [ hidden (mainPageData.local.state /= View)
                            , onClick (UpdateMainPageState Delete)
                            ]
                            [ text "Delete" ]
                        ]
                    , div [ hidden (mainPageData.local.state /= Add && mainPageData.local.state /= Edit) ]
                        [ select
                            [ name "New Category"
                            , on "change" (JD.map UpdateNewCategory targetValue)
                            ]
                            [ option [ value "Audio" ] [ text "Audio" ]
                            , option [ value "Book" ] [ text "Book" ]
                            , option [ value "Game" ] [ text "Game" ]
                            , option [ value "Video" ] [ text "Video" ]
                            ]
                        , label [] [ text "ID" ]
                        , input [ type_ "text", on "change" (JD.map UpdateNewID targetValue) ] []
                        , label [] [ text "Name" ]
                        , input [ type_ "text", on "change" (JD.map UpdateNewName targetValue) ] []
                        ]
                    ]
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


loadDB : String -> Cmd Msg
loadDB fileName =
    Http.post
        { url = baseUrl ++ "/load-database"
        , body = Http.jsonBody <| JE.object [ ( "database-file-name", JE.string fileName ) ]
        , expect = Http.expectJson GotMainPageData mainPageDataDecoder
        }


addVideo : String -> String -> Cmd Msg
addVideo id name =
    Http.post
        { url = baseUrl ++ "/add-video"
        , body = Http.jsonBody <| JE.object [ ( "id", JE.string id ), ( "name", JE.string name ) ]
        , expect = Http.expectJson GotMainPageData mainPageDataDecoder
        }



-- JSON


fileNameDecoder : JD.Decoder FileNames
fileNameDecoder =
    JD.list JD.string


mainPageDataDecoder : JD.Decoder MainPageReceivedData
mainPageDataDecoder =
    JD.map6 MainPageReceivedData
        (JD.field "selected-category" JD.string)
        (JD.field "db-date-created" JD.string)
        (JD.field "db-file-name" JD.string)
        (JD.field "db-date-updated" JD.string)
        (JD.field "entries" entriesDecoder)
        (JD.field "categories" (JD.list JD.string))


entriesDecoder : JD.Decoder (List Entry)
entriesDecoder =
    JD.list entryDecoder


entryDecoder : JD.Decoder Entry
entryDecoder =
    JD.map3 Entry
        (JD.field "category" JD.string)
        (JD.field "id" JD.string)
        (JD.field "name" JD.string)



-- converter
