-- Copyright © 2021-2022 Hovig Manjikian
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


module Page.Start exposing (Model, Msg, init, update, view)

import FileName exposing (FileName)
import FileNames exposing (FileNames)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import HttpSettings
import Json.Decode as JD



-- MODEL


type Model
    = Start
        { dbFileNames : FileNames
        , newDBFileName : Maybe FileName
        , selectedDBFileName : Maybe FileName
        }
    | Loading
        { dbFileNames : FileNames
        , newDBFileName : Maybe FileName
        , selectedDBFileName : Maybe FileName
        }
    | RetryGettingFileNames


init : ( Model, Cmd Msg )
init =
    ( Loading
        { dbFileNames = FileNames.init
        , newDBFileName = Nothing
        , selectedDBFileName = Nothing
        }
    , getFileNames
    )



-- UPDATE


type Msg
    = UpdateFileNames (Result Http.Error FileNames)
    | RefreshFileNames
    | UpdateSelectedDBFileName FileName
    | UpdateNewDBFileName FileName
    | LoadDB
    | CreateNewDB


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case ( msg, model ) of
        ( UpdateFileNames result, Loading m ) ->
            case result of
                Err _ ->
                    ( RetryGettingFileNames, Cmd.none )

                Ok dbFileNames ->
                    ( Start
                        { m | dbFileNames = dbFileNames }
                    , Cmd.none
                    )

        -- todo: Show spinner
        ( RefreshFileNames, Start m ) ->
            ( Start m, getFileNames )

        ( UpdateSelectedDBFileName fileName, Start m ) ->
            ( Start { m | selectedDBFileName = Just fileName }
            , Cmd.none
            )

        ( UpdateNewDBFileName fileName, Start m ) ->
            ( Start { m | newDBFileName = Just fileName }
            , Cmd.none
            )

        -- DoNothing/Imposible state
        ( _, _ ) ->
            ( model, Cmd.none )



-- VIEW


view : Model -> Html Msg
view model =
    case model of
        Loading _ ->
            div []
                [ h1 [] [ text "Loading..." ]
                ]

        Start m ->
            div []
                [ h1 [] [ text "COLLECTOR" ]
                , div []
                    [ h2 [] [ text "Select a database" ]
                    , select
                        [ name "Available databases"
                        , size 10
                        , on "change" (JD.map UpdateSelectedDBFileName FileName.decoder)
                        ]
                        (List.map
                            (\fileName ->
                                option [ value fileName ] [ text fileName ]
                            )
                            (FileNames.getAll m.dbFileNames)
                        )
                    , button
                        [ onClick RefreshFileNames ]
                        [ text "Refresh" ]
                    , button
                        [ onClick LoadDB
                        , disabled (not (isDBFileSelected model))
                        ]
                        [ text "Load Selected Database" ]
                    ]
                , div []
                    [ h2 [] [ text "Create a new database" ]
                    , input [ type_ "text", on "change" (JD.map UpdateNewDBFileName FileName.decoder) ] []
                    , button [ onClick CreateNewDB, disabled (not (isNewDBFileName model)) ] [ text "Create" ]
                    ]
                , div []
                    [ p [] [ text "To delete an existing database, delete the file from the file-system." ] ]
                ]

        RetryGettingFileNames ->
            div []
                [ h1 [] [ text "Retry..." ]
                ]



-- Getters


isDBFileSelected : Model -> Bool
isDBFileSelected model =
    case model of
        Start m ->
            case m.selectedDBFileName of
                Just _ ->
                    True

                Nothing ->
                    False

        _ ->
            False


isNewDBFileName : Model -> Bool
isNewDBFileName model =
    case model of
        Start m ->
            case m.newDBFileName of
                Just _ ->
                    True

                Nothing ->
                    False

        _ ->
            False



-- HTTP


getFileNames : Cmd Msg
getFileNames =
    Http.get
        { url = HttpSettings.baseUrl ++ "/get-available-database-files"
        , expect = Http.expectJson UpdateFileNames FileNames.decoder
        }
