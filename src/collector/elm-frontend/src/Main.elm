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


module Main exposing (main)

import Browser exposing (Document)
import Html exposing (..)
import Page.Start as Start


main : Program () Model Msg
main =
    Browser.document
        { init = init
        , view = view
        , update = update
        , subscriptions = \_ -> Sub.none
        }



-- MODEL


type Model
    = MainModel
        { page : Page
        }
    | ImpossibleStateReached


type Page
    = StartPage Start.Model


init : () -> ( Model, Cmd Msg )
init _ =
    let
        ( startPage, mappedPageCmds ) =
            let
                ( pageModel, pageCmds ) =
                    Start.init
            in
            ( StartPage pageModel, Cmd.map StartPageMsg pageCmds )
    in
    ( MainModel { page = startPage }, mappedPageCmds )



-- UPDATE


type Msg
    = StartPageMsg Start.Msg


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case model of
        MainModel m ->
            case ( msg, m.page ) of
                ( StartPageMsg subMsg, StartPage pageModel ) ->
                    let
                        ( updatedPageModel, pageCmd ) =
                            Start.update subMsg pageModel
                    in
                    if Start.isDBLoaded updatedPageModel then
                        ( ImpossibleStateReached, Cmd.none )

                    else
                        ( MainModel { m | page = StartPage updatedPageModel }
                        , Cmd.map StartPageMsg pageCmd
                        )

        ImpossibleStateReached ->
            ( model, Cmd.none )



-- VIEW


view : Model -> Document Msg
view model =
    case model of
        MainModel m ->
            { title = "Collector"
            , body = [ currentView m.page ]
            }

        ImpossibleStateReached ->
            { title = "Collector"
            , body =
                [ h1 [] [ text "Impossible State Reached!" ]
                ]
            }


currentView : Page -> Html Msg
currentView page =
    case page of
        StartPage pageModel ->
            Start.view pageModel
                |> Html.map StartPageMsg
