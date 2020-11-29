package com.durakonline.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

import eu.timepit.refined.auto._
import eu.timepit.refined.api.RefType

class RefinedTypesSpec extends AnyFlatSpec {
  "RoomName" should "allow strings shorter than 50 chars" in {
    RefType.applyRef[RoomName]("foo").isRight shouldBe true
  }

  it should "not allow strings longer than 50 chars" in {
    RefType.applyRef[RoomName]("a" * 51).isLeft shouldBe true
  }



  "RoomPassword" should "allow strings shorter than 50 chars" in {
    RefType.applyRef[RoomPassword]("foo").isRight shouldBe true
  }

  it should "allow strings that consist out of letters and digits" in {
    RefType.applyRef[RoomPassword]("foo123").isRight shouldBe true
  }

  it should "not allow strings longer than 50 chars" in {
    RefType.applyRef[RoomPassword]("a" * 51).isLeft shouldBe true
  }

  it should "not allow strings that contain other characters" in {
    RefType.applyRef[RoomPassword]("foo123%$#").isLeft shouldBe true
  }



  "UUIDString" should "allow UUID" in {
    RefType.applyRef[UUIDString]("123e4567-e89b-12d3-a456-556642440000")
      .isRight shouldBe true
  }
  
  it should "not allow non-UUID string" in {
    RefType.applyRef[UUIDString]("foo").isLeft shouldBe true
  }



  "UserName" should "allow strings shorter than 20 chars" in {
    RefType.applyRef[UserName]("foo").isRight shouldBe true
  }

  it should "not allow strings longer than 20 chars" in {
    RefType.applyRef[UserName]("a" * 51).isLeft shouldBe true
  }
}
