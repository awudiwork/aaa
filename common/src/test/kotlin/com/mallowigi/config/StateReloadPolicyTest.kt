/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2024 Elior "Mallowigi" Boukhobza
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mallowigi.config

import com.mallowigi.icons.associations.RegexAssociation
import com.mallowigi.icons.associations.SelectedAssociations
import com.mallowigi.models.IconType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StateReloadPolicyTest {

  /** 测试用的最小持久化状态。 */
  class SampleState {
    var name: String = ""
    var items: MutableList<String> = mutableListOf()
  }

  private fun sampleState(name: String, vararg items: String) = SampleState().apply {
    this.name = name
    this.items = items.toMutableList()
  }

  private fun regexAssociation(name: String, pattern: String) = RegexAssociation().apply {
    this.name = name
    this.icon = "/icons/$name.svg"
    this.pattern = pattern
    this.touched = true
  }

  private fun selectedFileAssociations(vararg associations: RegexAssociation) = SelectedAssociations(IconType.FILE).apply {
    associationsList = associations.toMutableList()
  }

  @Test
  fun `首次加载即使内容不同也不广播`() {
    val current = sampleState("default")
    val loaded = sampleState("persisted", "a", "b")

    assertFalse(StateReloadPolicy.shouldNotify(isInitialLoad = true, current = current, loaded = loaded))
  }

  @Test
  fun `重新加载且内容相同则不广播`() {
    val current = sampleState("same", "a", "b")
    val loaded = sampleState("same", "a", "b")

    assertFalse(StateReloadPolicy.shouldNotify(isInitialLoad = false, current = current, loaded = loaded))
  }

  @Test
  fun `重新加载且内容不同则广播`() {
    val current = sampleState("same", "a")
    val loaded = sampleState("same", "a", "b")

    assertTrue(StateReloadPolicy.shouldNotify(isInitialLoad = false, current = current, loaded = loaded))
  }

  @Test
  fun `不同实例但关联内容相同视为同一状态`() {
    val current = selectedFileAssociations(regexAssociation(name = "Foo", pattern = "foo"))
    val loaded = selectedFileAssociations(regexAssociation(name = "Foo", pattern = "foo"))

    assertTrue(StateReloadPolicy.isSamePersistentState(current, loaded))
  }

  @Test
  fun `关联内容变化视为不同状态`() {
    val current = selectedFileAssociations(regexAssociation(name = "Foo", pattern = "foo"))
    val loaded = selectedFileAssociations(regexAssociation(name = "Foo", pattern = "bar"))

    assertFalse(StateReloadPolicy.isSamePersistentState(current, loaded))
  }
}
