module DataStructures.Entry exposing (Entry, decoder)

import Json.Decode as JD


type alias Entry =
    { category : String
    , id : String
    , name : String
    }


decoder : JD.Decoder Entry
decoder =
    JD.map3 Entry
        (JD.field "category" JD.string)
        (JD.field "id" JD.string)
        (JD.field "name" JD.string)
