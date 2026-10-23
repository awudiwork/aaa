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

import com.intellij.openapi.util.JDOMUtil
import com.intellij.util.xmlb.XmlSerializer

/**
 * 决定 PersistentStateComponent 在 loadState 时是否需要广播配置变更。
 *
 * 规则：
 * - 首次加载（组件初始化）只是恢复已持久化的状态，不广播；
 * - 之后的重新加载（Settings Sync、VCS 更新、外部修改配置文件）仅在持久化内容确实不同时广播。
 *
 * 以 XML 序列化结果作为比较依据，与平台实际持久化的内容保持单一事实来源，
 * 避免依赖各状态类自行实现 equals。
 */
object StateReloadPolicy {

  /**
   * @param isInitialLoad 是否为组件的首次状态加载
   * @param current 加载前组件当前持有的状态
   * @param loaded 平台传入的待加载状态
   * @return true 表示需要广播配置变更
   */
  fun shouldNotify(isInitialLoad: Boolean, current: Any, loaded: Any): Boolean = when {
    isInitialLoad -> false
    else          -> !isSamePersistentState(current, loaded)
  }

  /** 两个状态对象序列化后的 XML 是否一致。 */
  fun isSamePersistentState(first: Any, second: Any): Boolean =
    JDOMUtil.areElementsEqual(XmlSerializer.serialize(first), XmlSerializer.serialize(second))
}
