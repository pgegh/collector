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
        , view = view
        , update = update
        , subscriptions = subscriptions
        }



-- MODEL


type Model
    = Loading
    | StartPage StartPageData
    | ErrorDBFileNames
    | MainPage MainPageData
    | ErrorPage String


type alias StartPageData =
    { dbFileNames : FileNames
    , selectedDBFile : String
    , newDBFileName : String
    }


type alias MainPageData =
    { category : String
    , entries : List Entry
    }


type alias Entry =
    { id : String
    , name : String
    , category : String
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
    | GotMainPageData (Result Http.Error MainPageData)
    | LoadDB
    | UpdateNewDBFileName String
    | UpdateSelectedCategory String
    | UpdateSelectedDBFileName String


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
            ( MainPage { entries = [], category = "All" }, loadDB fileName )

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

                Ok mainPageData ->
                    ( MainPage mainPageData, Cmd.none )

        LoadDB ->
            let
                fileName =
                    case model of
                        StartPage spData ->
                            spData.selectedDBFile

                        _ ->
                            ""
            in
            ( MainPage { entries = [], category = "All" }, loadDB fileName )

        UpdateNewDBFileName fileName ->
            case model of
                StartPage startPageData ->
                    ( StartPage { startPageData | newDBFileName = fileName }, Cmd.none )

                _ ->
                    ( ErrorPage "Trying to change new database file name when not in StartPage.", Cmd.none )

        UpdateSelectedCategory category ->
            case model of
                MainPage mainPageData ->
                    ( MainPage { mainPageData | category = category }, Cmd.none )

                _ ->
                    ( ErrorPage "Trying to change category when not in MainPage.", Cmd.none )

        UpdateSelectedDBFileName fileName ->
            case model of
                StartPage startPageData ->
                    ( StartPage { startPageData | selectedDBFile = fileName }, Cmd.none )

                _ ->
                    ( ErrorPage "Trying to update selected database file name when not in StartPage.", Cmd.none )



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
                [ div []
                    [ select
                        [ name "Categories" ]
                        [ option [ value "All" ] [ text "All" ]
                        , option [ value "Movies" ] [ text "Movies" ]
                        ]
                    ]
                , div []
                    [ select
                        [ name "Entries" ]
                        (List.map
                            (\entry ->
                                option [ value entry.id ] [ text entry.name ]
                            )
                            mainPageData.entries
                        )
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



-- JSON


fileNameDecoder : JD.Decoder FileNames
fileNameDecoder =
    JD.list JD.string


mainPageDataDecoder : JD.Decoder MainPageData
mainPageDataDecoder =
    JD.map2 MainPageData
        (JD.field "category" JD.string)
        (JD.field "entries" entriesDecoder)


entriesDecoder : JD.Decoder (List Entry)
entriesDecoder =
    JD.list entryDecoder


entryDecoder : JD.Decoder Entry
entryDecoder =
    JD.map3 Entry
        (JD.field "id" JD.string)
        (JD.field "name" JD.string)
        (JD.field "category" JD.string)



-- converter
