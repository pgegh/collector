module DB exposing (DB, decoder)

import Entry exposing (Entry)
import FileName exposing (FileName)
import Json.Decode as JD


type alias DB =
    { fileName : FileName
    , dbCreatedDate : String
    , dbUpdatedDate : String
    , catagories : List String
    , selectedCategory : String
    , entries : List Entry
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
