/* 
 * Copyright 2011 Untyped Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bigtop
package mongodb

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.json.JsonParser._
import net.liftweb.util._

class BaseUserSuite extends BaseSuite {
  
  def expectLoggedOut = {
    expect(None)(User.realUsername)
    expect(None)(User.realUser)
    expect(None)(User.effectiveUsername)
    expect(None)(User.effectiveUser)
  }

  val session = new LiftSession("", StringHelpers.randomString(20), Empty)
  
  test("password.match_?") {
    val user1 = User.createRecord.password("password")
    expect(true)(user1.password.match_?("password"))
    expect(false)(user1.password.match_?("wordpass"))

    val user2 = User.createRecord.password("wordpass")
    expect(true)(user2.password.match_?("wordpass"))
    expect(false)(user2.password.match_?("password"))
  }
  
  test("password is same after serialisation") {
    val user1 = User.createRecord.username("dave").password("password")
    val hash = user1.password.is
    user1.save

    expect(hash){ User.byUsername("dave").get.password.is }
  }

  test("byUsernameAndPassword") {
    S.initIfUninitted(session) {
      val user1 = User.createRecord.
                       username("dave").
                       email("dave@dave.com").
                       password("password").
                       save
    
      expect(Some(user1))(User.byUsername("dave"))
      expect(Some(user1))(User.byUsernameAndPassword("dave", "password"))
      expect(None)(User.byUsernameAndPassword("dave@dave.com", "password"))
      expect(None)(User.byUsernameAndPassword("dave", "passwork"))
    }
  }
  
  test("byEmailVerificationCode") {
    S.initIfUninitted(session) {
      val user1 = User.createRecord.
                       username("dave").
                       email("dave@dave.com").
                       password("password").
                       emailVerificationCode("abc").
                       save

      val user2 = User.createRecord.
                       username("noel").
                       email("noel@noel.com").
                       password("password").
                       emailVerificationCode("def").
                       emailVerified(true).
                       save

      val user3 = User.createRecord.
                       username("laura").
                       email("laura@laura.com").
                       password("password").
                       save
    
      expect(Some(user1))(User.byEmailVerificationCode("abc"))
      expect(Some(user2))(User.byEmailVerificationCode("def"))
      expect(None)(User.byEmailVerificationCode(""))
    }
  }
  
  test("logIn, logOut") {
    S.initIfUninitted(session) {
      val user1 = User.createRecord.
                       username("dave").
                       email("dave@dave.com").
                       password("password").
                       save

      expectLoggedOut
    
      User.logIn(user1)
    
      expect(Some(user1.username.is))(User.realUsername)
      expect(Some(user1))(User.realUser)
      expect(Some(user1.username.is))(User.effectiveUsername)
      expect(Some(user1))(User.effectiveUser)
    
      User.logOut
    
      expectLoggedOut
    }
  }
  
  test("changeIdentity, restoreIdentity") {
    S.initIfUninitted(session) {
      val user1 = User.createRecord.
                       username("dave").
                       email("dave@dave.com").
                       password("password").
                       superuser(true).
                       save
      val user2 = User.createRecord.
                       username("noel").
                       email("noel@noel.com").
                       password("password").
                       superuser(false).
                       save

      expectLoggedOut
    
      // Log in as superuser and change identity - change succeeds:
    
      User.logIn(user1)
      expect(true)(User.changeIdentity(user2))
    
      expect(Some(user1.username.is))(User.realUsername)
      expect(Some(user1))(User.realUser)
      expect(Some(user2.username.is))(User.effectiveUsername)
      expect(Some(user2))(User.effectiveUser)
      
      // User.restoreIdentity would work, but we want to check that changeIdentity
      // is checking the real user's permissions, not the effective ones:
      User.changeIdentity(user1)
      
      expect(Some(user1.username.is))(User.realUsername)
      expect(Some(user1))(User.realUser)
      expect(Some(user1.username.is))(User.effectiveUsername)
      expect(Some(user1))(User.effectiveUser)

      User.logOut
    
      expectLoggedOut
      
      // Log in as non-superuser and change identity - change fails:
    
      User.logIn(user2)
      expect(false)(User.changeIdentity(user1))
      
      expect(Some(user2.username.is))(User.realUsername)
      expect(Some(user2))(User.realUser)
      expect(Some(user2.username.is))(User.effectiveUsername)
      expect(Some(user2))(User.effectiveUser)
      
      User.logOut
    
      expectLoggedOut
    }
  }
  
}
