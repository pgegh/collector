module DataStructures.EntryData exposing (EntryData, audioDecoder, bookDecoder, gameDecoder, initAudio, initBook, initGame, initVideo, videoDecoder)

import Json.Decode as JD


type EntryData
    = Audio
        { name : String
        , artists : List String
        , year : Int
        , languages : List String
        , genre : String
        }
    | Book
        { name : String
        , authors : List String
        , publisher : String
        , year : Int
        , language : String
        , categories : List String
        }
    | Game
        { name : String
        , year : Int
        , companies : List String
        , platform : String
        , genres : List String
        }
    | Video
        { name : String
        , originalTitle : String
        , year : Int
        , languages : List String
        , countries : List String
        , genres : List String
        }



-- Json


initAudio_ : String -> List String -> Int -> List String -> String -> EntryData
initAudio_ name artists year languages genre =
    Audio
        { name = name
        , artists = artists
        , year = year
        , languages = languages
        , genre = genre
        }
initAudio : EntryData
initAudio =
    Audio
        { name = ""
        , artists = []
        , year = 0
        , languages = []
        , genre = ""
        }


audioDecoder : JD.Decoder EntryData
audioDecoder =
    JD.map5 initAudio_
        (JD.field "name" JD.string)
        (JD.field "artists" (JD.list JD.string))
        (JD.field "year" JD.int)
        (JD.field "languages" (JD.list JD.string))
        (JD.field "genre" JD.string)


initBook_ : String -> List String -> String -> Int -> String -> List String -> EntryData
initBook_ name authors publisher year language categories =
    Book
        { name = name
        , authors = authors
        , publisher = publisher
        , year = year
        , language = language
        , categories = categories
        }
initBook : EntryData
initBook =
    Book
        { name = ""
        , authors = []
        , publisher = ""
        , year = 0
        , language = ""
        , categories = []
        }


bookDecoder : JD.Decoder EntryData
bookDecoder =
    JD.map6 initBook_
        (JD.field "name" JD.string)
        (JD.field "authors" (JD.list JD.string))
        (JD.field "publisher" JD.string)
        (JD.field "year" JD.int)
        (JD.field "language" JD.string)
        (JD.field "categories" (JD.list JD.string))


initGame_ : String -> Int -> List String -> String -> List String -> EntryData
initGame_ name year companies platform genres =
    Game
        { name = name
        , year = year
        , companies = companies
        , platform = platform
        , genres = genres
        }
initGame : EntryData
initGame =
    Game
        { name = ""
        , year = 0
        , companies = []
        , platform = ""
        , genres = []
        }


gameDecoder : JD.Decoder EntryData
gameDecoder =
    JD.map5 initGame_
        (JD.field "name" JD.string)
        (JD.field "year" JD.int)
        (JD.field "companies" (JD.list JD.string))
        (JD.field "platform" JD.string)
        (JD.field "genres" (JD.list JD.string))


initVideo_ : String -> String -> Int -> List String -> List String -> List String -> EntryData
initVideo_ name originalTitle year languages countries genres =
    Video
        { name = name
        , originalTitle = originalTitle
        , year = year
        , languages = languages
        , countries = countries
        , genres = genres
        }
initVideo : EntryData
initVideo =
    Video
        { name = ""
        , originalTitle = ""
        , year = 0
        , languages = []
        , countries = []
        , genres = []
        }


videoDecoder : JD.Decoder EntryData
videoDecoder =
    JD.map6 initVideo_
        (JD.field "name" JD.string)
        (JD.field "original-title" JD.string)
        (JD.field "year" JD.int)
        (JD.field "languages" (JD.list JD.string))
        (JD.field "countries" (JD.list JD.string))
        (JD.field "genres" (JD.list JD.string))
