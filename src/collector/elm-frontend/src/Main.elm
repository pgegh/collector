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


type alias Model =
    { page : Page
    }


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
    ( { page = startPage }, mappedPageCmds )



-- UPDATE


type Msg
    = StartPageMsg Start.Msg


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case ( msg, model.page ) of
        ( StartPageMsg subMsg, StartPage pageModel ) ->
            let
                ( updatedPageModel, pageCmd ) =
                    Start.update subMsg pageModel
            in
            ( { model | page = StartPage updatedPageModel }
            , Cmd.map StartPageMsg pageCmd
            )



-- VIEW


view : Model -> Document Msg
view model =
    { title = "Collector"
    , body = [ currentView model ]
    }


currentView : Model -> Html Msg
currentView model =
    case model.page of
        StartPage pageModel ->
            Start.view pageModel
                |> Html.map StartPageMsg
