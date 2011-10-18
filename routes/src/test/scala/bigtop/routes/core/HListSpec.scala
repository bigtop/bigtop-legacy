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

package bigtop.routes.core

import org.specs._

class HListSpec extends Specification {

  "HLists can have heterogeneous arguments" in {
    val list = 2.0 :: 1 :: HNil
    
    list.head mustEqual 2.0
    list.tail mustEqual (1 :: HNil)
    
    list.tail.head mustEqual 1
    list.tail.tail mustEqual HNil
  }
  
} 
