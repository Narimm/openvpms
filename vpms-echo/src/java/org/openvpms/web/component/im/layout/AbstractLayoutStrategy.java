package org.openvpms.web.component.im.layout;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import echopointng.DateField;
import echopointng.TabbedPane;
import echopointng.tabbedpane.DefaultTabModel;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.button.AbstractButton;
import nextapp.echo2.app.text.TextComponent;
import org.apache.commons.lang.StringUtils;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;


/**
 * Abstract implementation of the {@link IMObjectLayoutStrategy} interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractLayoutStrategy implements IMObjectLayoutStrategy {

    /**
     * Map of node descriptors to their corresponding components, used to set
     * focus.
     */
    private final Map<NodeDescriptor, Component> _components
            = new LinkedHashMap<NodeDescriptor, Component>();

    /**
     * Construct a new <code>AbstractLayoutStrategy</code>.
     */
    public AbstractLayoutStrategy() {
    }

    /**
     * Apply the layout strategy.
     *
     * @param object  the object to apply
     * @param context
     * @return the component containing the rendered <code>object</code>
     */
    public Component apply(IMObject object, LayoutContext context) {
        _components.clear();
        Column column = ColumnFactory.create("CellSpacing");
        doLayout(object, column, context);
        setFocus(object, column);
        return column;
    }

    /**
     * Lay out out the object in the specified container.
     *
     * @param object    the object to lay out
     * @param container the container to use
     * @param context
     */
    protected void doLayout(IMObject object, Component container,
                            LayoutContext context) {
        ArchetypeDescriptor descriptor
                = DescriptorHelper.getArchetypeDescriptor(object);
        List<NodeDescriptor> simple;
        List<NodeDescriptor> complex;

        NodeFilter filter = getNodeFilter(context);
        simple = filter(object, descriptor.getSimpleNodeDescriptors(), filter);
        complex = filter(object, descriptor.getComplexNodeDescriptors(), filter);

        doSimpleLayout(object, simple, container, context);
        doComplexLayout(object, complex, container, context);
    }

    /**
     * Lays out child components in a 2x2 grid.
     *
     * @param object      the parent object
     * @param descriptors the child descriptors
     * @param container   the container to use
     * @param context
     */
    protected void doSimpleLayout(IMObject object,
                                  List<NodeDescriptor> descriptors,
                                  Component container,
                                  LayoutContext context) {
        if (!descriptors.isEmpty()) {
            Grid grid = GridFactory.create(4);
            doGridLayout(object, descriptors, grid, context);
            container.add(grid);
        }
    }

    /**
     * Lays out each child component in a tabbed pane.
     *
     * @param object      the parent object
     * @param descriptors the child descriptors
     * @param container   the container to use
     * @param context
     */
    protected void doComplexLayout(IMObject object,
                                   List<NodeDescriptor> descriptors,
                                   Component container,
                                   LayoutContext context) {
        if (!descriptors.isEmpty()) {
            DefaultTabModel model = new DefaultTabModel();
            for (NodeDescriptor nodeDesc : descriptors) {
                Component child = createComponent(object, nodeDesc, context);

                DefaultTabModel.TabButton button
                        = model.new TabButton(nodeDesc.getDisplayName(), null);
                button.setFocusTraversalParticipant(false);
                // @todo - button doesn't respond to keypress, so don't focus
                // on it.

                Component inset = ColumnFactory.create("Inset", child);
                model.insertTab(model.size(), button, inset);
            }
            TabbedPane pane = new TabbedPane();
            pane.setModel(model);
            pane.setSelectedIndex(0);
            container.add(pane);
        }
    }

    /**
     * Returns a node filter to filter nodes. This implementation return {@link
     * LayoutContext#getDefaultNodeFilter()}.
     *
     * @param context the context
     * @return a node filter to filter nodes, or <code>null</code> if no
     *         filterering is required
     */
    protected NodeFilter getNodeFilter(LayoutContext context) {
        return context.getDefaultNodeFilter();
    }

    /**
     * Helper to create a chained node filter from the default node filter and a
     * custom node filter.
     *
     * @param context the context
     * @param filter  the node filter
     */
    protected ChainedNodeFilter getNodeFilter(LayoutContext context,
                                              NodeFilter filter) {
        return FilterHelper.chain(context.getDefaultNodeFilter(), filter);
    }

    /**
     * Filters a set of node descriptors, using the specfied node filter.
     *
     * @param object      the object
     * @param descriptors the node descriptors to filter
     * @param filter      the filter to use
     * @return the filtered nodes
     */
    protected List<NodeDescriptor> filter(IMObject object,
                                          List<NodeDescriptor> descriptors,
                                          NodeFilter filter) {
        return FilterHelper.filter(object, filter, descriptors);
    }

    /**
     * Lays out child components in 2 columns.
     *
     * @param object      the parent object
     * @param descriptors the child descriptors
     * @param grid        the grid to use
     * @param context     the layout context
     */
    protected void doGridLayout(IMObject object,
                                List<NodeDescriptor> descriptors, Grid grid,
                                LayoutContext context) {
        int size = descriptors.size();
        Component[] components = new Component[size];
        String[] labels = new String[components.length];
        for (int i = 0; i < components.length; ++i) {
            NodeDescriptor descriptor = descriptors.get(i);
            labels[i] = descriptor.getDisplayName();
            Component component = createComponent(object, descriptor, context);
            setTabIndex(component, context);
            components[i] = component;
        }

        int rows = (size / 2) + (size % 2);
        for (int i = 0, j = rows; i < rows; ++i, ++j) {
            add(grid, labels[i], components[i]);
            if (j < size) {
                add(grid, labels[j], components[j]);
            }
        }
    }

    /**
     * Helper to add a node to a container.
     *
     * @param container the container
     * @param name      the node display name
     * @param component the component representing the node
     */
    protected void add(Component container, String name, Component component) {
        Label label = LabelFactory.create();
        label.setText(name);
        container.add(label);
        container.add(component);
    }

    /**
     * Helper to add a node to a container, setting its tab index.
     *
     * @param container the container
     * @param name      the node display name
     * @param component the component representing the node
     * @param context   the layout context
     */
    protected void add(Component container, String name, Component component,
                       LayoutContext context) {
        add(container, name, component);
        setTabIndex(component, context);
    }

    /**
     * Creates a component for a node descriptor. This maintains a cache of
     * created components, in order for the focus to be set on an appropriate
     * component.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor
     * @param context    the layout context
     */
    protected Component createComponent(IMObject parent,
                                        NodeDescriptor descriptor,
                                        LayoutContext context) {
        IMObjectComponentFactory factory = context.getComponentFactory();
        Component component = factory.create(parent, descriptor);
        _components.put(descriptor, component);
        return component;
    }

    /**
     * Sets focus on the first focusable field.
     *
     * @param object    the object
     * @param container the component container
     */
    protected void setFocus(IMObject object, Component container) {
        Component focusable = null;
        for (Map.Entry<NodeDescriptor, Component> entry :
                _components.entrySet()) {
            Component child = entry.getValue();
            if (child instanceof TextComponent || child instanceof CheckBox
                || child instanceof DateField
                || child instanceof AbstractButton) {
                if (child.isEnabled() && child.isFocusTraversalParticipant()) {
                    NodeDescriptor descriptor = entry.getKey();
                    try {
                        Object value = descriptor.getValue(object);
                        if (value == null
                            || (value instanceof String
                                && StringUtils.isEmpty((String) value))) {
                            // null field. Set focus on it in preference to
                            // others
                            focusable = child;
                            break;
                        } else {
                            if (focusable == null) {
                                focusable = child;
                            }
                        }
                    } catch (DescriptorException ignore) {
                    }
                }
            }
        }
        if (focusable != null) {
            if (focusable instanceof DateField) {
                // @todo - workaround
                focusable = ((DateField) focusable).getTextField();
            }
            ApplicationInstance.getActive().setFocusedComponent(focusable);
        }
    }

    /**
     * Sets the tab index of a component, if it is a focus traversal
     * participant.
     *
     * @param component the component
     * @param context   the layout context
     */
    protected void setTabIndex(Component component, LayoutContext context) {
        if (component.isFocusTraversalParticipant()) {
            context.setTabIndex(component);
        }
    }

}
