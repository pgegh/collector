module Main exposing (main)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)


main : Program () Model Msg
main =
    Browser.document
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }



-- MODEL


type alias Model =
    { pageTitle : String
    }


init : () -> ( Model, Cmd Msg )
init _ =
    ( Model "Collector - Set Database file", Cmd.none )



-- UPDATE


type Msg
    = Load
    | Create


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Load ->
            ( model, Cmd.none )

        Create ->
            ( model, Cmd.none )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none



-- VIEW


view : Model -> Browser.Document Msg
view model =
    { title = model.pageTitle
    , body =
        [ h1 [] [ text "COLLECTOR" ]
        , h2 [] [ text "Select a database" ]
        , select [ name "Available databases", size 10 ]
            --Some dummy data
            [ option [ value "1" ] [ text "First Choice" ]
            , option [ value "2" ] [ text "Second Choice" ]
            , option [ value "3" ] [ text "Third Choice" ]
            ]
        , div []
            [ button [ onClick Load ] [ text "Load Selected Database" ]
            , button [ onClick Create ] [ text "Create New Database" ]
            ]
        ]
    }
