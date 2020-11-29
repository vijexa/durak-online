package com.durakonline

import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.Size
import eu.timepit.refined.numeric.Less
import eu.timepit.refined.string._
import eu.timepit.refined.boolean.AllOf
import shapeless.HNil
import shapeless.::

package object model {
  type RoomName = String Refined Size[Less[50]] 
  type RoomPassword = String Refined AllOf[
    MatchesRegex["[A-z0-9]+"] :: 
    Size[Less[50]] :: 
    HNil]

  type UUIDString = String Refined Uuid
  type UserName = String Refined Size[Less[20]]
}
