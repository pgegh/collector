module DB exposing (DB, decoder, init)

import Entry exposing (Entry)
import FileName exposing (FileName)
import Json.Decode as JD


type alias DB =
    { fileName : FileName
    , dbCreatedDate : String
    , dbUpdatedDate : String
    , categories : List String
    , selectedCategory : String
    , entries : List Entry
    }



-- todo: Unnecessary function


init : DB
init =
    { fileName = FileName.init
    , dbCreatedDate = ""
    , dbUpdatedDate = ""
    , categories = []
    , selectedCategory = ""
    , entries = []
    }


decoder : JD.Decoder DB
decoder =
    JD.map6 DB
        (JD.field "db-file-name" FileName.decoder)
        (JD.field "db-date-created" JD.string)
        (JD.field "db-date-updated" JD.string)
        (JD.field "categories" (JD.list JD.string))
        (JD.field "selected-category" JD.string)
        (JD.field "entries" (JD.list Entry.decoder))
