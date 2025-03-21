// Copyright 2011-2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CMousePressedHandler;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitBendsTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitEdgeLabelsTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitEdgesTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitNodesTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;
import y.view.EdgeLabel;
import y.view.HitInfo;

public class CEdgeLabelExitState<
        NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IMouseState {
  /** State factory that creates new state objects when necessary. */
  private final CStateFactory<NodeType, EdgeType> m_factory;

  /** The graph the entered node belongs to. */
  private final AbstractZyGraph<NodeType, EdgeType> m_graph;

  /** The entered label. */
  private final EdgeLabel m_label;

  /**
   * Creates a new state object.
   *
   * @param factory a CStateFactory that creates new state objects when necessary.
   * @param graph the graph the entered node belongs to
   * @param label the associated edge label
   */
  public CEdgeLabelExitState(
      final CStateFactory<NodeType, EdgeType> factory,
      final AbstractZyGraph<NodeType, EdgeType> graph,
      final EdgeLabel label) {
    m_factory = Preconditions.checkNotNull(factory, "Error: factory argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "Error: graph argument can not be null");
    m_label = Preconditions.checkNotNull(label, "Error: label argument can not be null");
  }

  /**
   * Returns the graph the exited edge label belongs to.
   *
   * @return The graph the exited edge label belongs to.
   */
  public AbstractZyGraph<NodeType, EdgeType> getGraph() {
    return m_graph;
  }

  public EdgeLabel getLabel() {
    return m_label;
  }

  @Override
  public CStateFactory<NodeType, EdgeType> getStateFactory() {
    return m_factory;
  }

  @Override
  public IMouseStateChange mouseDragged(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(m_factory.createDefaultState(), true);
  }

  @Override
  public IMouseStateChange mouseMoved(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    final double x = m_graph.getEditMode().translateX(event.getX());
    final double y = m_graph.getEditMode().translateY(event.getY());

    final HitInfo hitInfo = m_graph.getGraph().getHitInfo(x, y);

    if (hitInfo.hasHitNodes()) {
      return CHitNodesTransformer.enterNode(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitNodeLabels()) {
      throw new IllegalStateException();
    } else if (hitInfo.hasHitEdges()) {
      return CHitEdgesTransformer.enterEdge(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitEdgeLabels()) {
      return CHitEdgeLabelsTransformer.enterEdgeLabel(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitBends()) {
      return CHitBendsTransformer.enterBend(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitPorts()) {
      return new CStateChange(this, true);
    } else {
      // We are in the exit state. All GUI changes have been undone and once
      // again we are not hitting anything. That means we can now return to
      // the default state.

      return new CStateChange(m_factory.createDefaultState(), true);
    }
  }

  @Override
  public IMouseStateChange mousePressed(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return CMousePressedHandler.handleMousePressed(m_factory, this, graph, event);
  }

  @Override
  public IMouseStateChange mouseReleased(
      final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(m_factory.createDefaultState(), true);
  }
}
