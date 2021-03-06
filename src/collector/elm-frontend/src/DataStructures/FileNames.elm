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


module DataStructures.FileNames exposing (FileNames, decoder, encoder, getAll, init)

import DataStructures.FileName as FileName exposing (FileName)
import Json.Decode as JD
import Json.Encode as JE


type FileNames
    = FileNames (List FileName)


init : FileNames
init =
    FileNames []



-- Getters


getAll : FileNames -> List FileName
getAll (FileNames fileNames) =
    fileNames



-- Json


decoder : JD.Decoder FileNames
decoder =
    JD.map FileNames (JD.list FileName.decoder)


encoder : FileNames -> JE.Value
encoder (FileNames listOfFilenames) =
    JE.list FileName.encoder listOfFilenames
