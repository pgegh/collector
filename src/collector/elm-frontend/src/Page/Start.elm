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
        { dbFileNames : FileNamesState
        , newDBFileName : Maybe FileName
        , selectedDBFileName : Maybe FileName
        }
    | ImpossibleStateReached


type FileNamesState
    = GotFileNames FileNames
    | GettingFileNames
    | FailedGettingFileNames


init : ( Model, Cmd Msg )
init =
    ( Start
        { dbFileNames = GettingFileNames
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
        ( UpdateFileNames result, Start m ) ->
            case result of
                Err _ ->
                    ( Start
                        { m | dbFileNames = FailedGettingFileNames }
                    , Cmd.none
                    )

                Ok dbFileNames ->
                    ( Start
                        { m | dbFileNames = GotFileNames dbFileNames }
                    , Cmd.none
                    )

        -- todo: Show spinner
        ( RefreshFileNames, Start m ) ->
            ( Start { m | dbFileNames = GettingFileNames }
            , getFileNames
            )

        ( UpdateSelectedDBFileName fileName, Start m ) ->
            ( Start { m | selectedDBFileName = Just fileName }
            , Cmd.none
            )

        ( UpdateNewDBFileName fileName, Start m ) ->
            ( Start { m | newDBFileName = Just fileName }
            , Cmd.none
            )

        -- DoNothing/Impossible state
        ( _, _ ) ->
            ( ImpossibleStateReached, Cmd.none )



-- VIEW


view : Model -> Html Msg
view model =
    case model of
        Start m ->
            div []
                [ h1 [] [ text "COLLECTOR" ]
                , div []
                    [ h2 [] [ text "Select a database" ]
                    , case m.dbFileNames of
                        GettingFileNames ->
                            label [] [ text "Loading" ]

                        GotFileNames fileNames ->
                            select
                                [ name "Available databases"
                                , size 10
                                , on "change" (JD.map UpdateSelectedDBFileName FileName.decoder)
                                ]
                                (List.map
                                    (\fileName ->
                                        option [ value fileName ] [ text fileName ]
                                    )
                                    (FileNames.getAll fileNames)
                                )

                        FailedGettingFileNames ->
                            label [] [ text "Could not retrieve file names from the server" ]
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

        ImpossibleStateReached ->
            div []
                [ h1 [] [ text "Impossible State Reached!" ]
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
