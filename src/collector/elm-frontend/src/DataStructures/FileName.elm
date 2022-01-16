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


module DataStructures.FileName exposing (FileName, decoder, encoder, getString, init)

import Json.Decode as JD
import Json.Encode as JE


type FileName
    = FileName String


init : FileName
init =
    FileName ""



-- Getters


getString : FileName -> String
getString (FileName str) =
    str



-- Json


decoder : JD.Decoder FileName
decoder =
    JD.map FileName JD.string


encoder : FileName -> JE.Value
encoder (FileName str) =
    JE.string str
