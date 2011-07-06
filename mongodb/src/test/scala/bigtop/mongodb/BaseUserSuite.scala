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
    expect(Empty)(User.realUsername)
    expect(Empty)(User.realUser)
    expect(Empty)(User.effectiveUsername)
    expect(Empty)(User.effectiveUser)
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
  
  test("byUsernameAndPassword") {
    S.initIfUninitted(session) {
      val user1 = User.createRecord.
                       username("dave").
                       email("dave@dave.com").
                       password("password").
                       save
    
      expect(Some(user1))(User.byUsername("dave").get)
      expect(Some(user1))(User.byUsernameAndPassword("dave", "password"))
      expect(None)(User.byUsernameAndPassword("dave@dave.com", "password"))
      expect(None)(User.byUsernameAndPassword("dave", "passwork"))
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
    
      expect(Full(user1.username.is))(User.realUsername)
      expect(Full(user1))(User.realUser)
      expect(Full(user1.username.is))(User.effectiveUsername)
      expect(Full(user1))(User.effectiveUser)
    
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
                       save
      val user2 = User.createRecord.
                       username("noel").
                       email("noel@noel.com").
                       password("password").
                       save

      expectLoggedOut
    
      User.logIn(user1)
      User.changeIdentity(user2)
    
      expect(Full(user1.username.is))(User.realUsername)
      expect(Full(user1))(User.realUser)
      expect(Full(user2.username.is))(User.effectiveUsername)
      expect(Full(user2))(User.effectiveUser)
    
      User.restoreIdentity
      
      expect(Full(user1.username.is))(User.realUsername)
      expect(Full(user1))(User.realUser)
      expect(Full(user1.username.is))(User.effectiveUsername)
      expect(Full(user1))(User.effectiveUser)

      User.logOut
    
      expectLoggedOut
    }
  }
  
}