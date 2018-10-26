package org.terifan.bundle.dev;

import java.util.ArrayDeque;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import org.terifan.bundle.old.Bundle;
import org.terifan.bundle.old.BundleVisitor;


public class Editor
{
	public static void main(String ... args)
	{
		try
		{
			Bundle bundle = new Bundle();
			bundle.putString("key", "value");
			bundle.putBundle("bundle", new Bundle()
				.putString("key", "value")
			);

			bundle = BundleGenerator.create();

			DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TreeNode(bundle, null));

			ArrayDeque<DefaultMutableTreeNode> stack = new ArrayDeque<>();
			ArrayDeque<DefaultMutableTreeNode> arrays = new ArrayDeque<>();
			stack.add(root);

			bundle.visit(new BundleVisitor()
				{
					@Override
					public void enteringArray(int aIndex0, int aIndex1)
					{
					}

					@Override
					public void leavingArray(int aIndex0, int aIndex1)
					{
					}

					@Override
					public void entering(Bundle aParentBundle, String aKey, Bundle aChildBundle, int aIndex)
					{
						if (aIndex == 0)
						{
							DefaultMutableTreeNode node = new DefaultMutableTreeNode(aKey);
							stack.peekLast().add(node);
							stack.add(node);

							arrays.add(node);
						}

						if (aIndex > -1)
						{
							DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeNode(aChildBundle, ""+aIndex));
							arrays.peekLast().add(node);
							stack.add(node);
						}
						else
						{
							DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeNode(aChildBundle, aKey));
							stack.peekLast().add(node);
							stack.add(node);
						}
					}
					@Override
					public void leaving(Bundle aParentBundle, String aKey, Bundle aChildBundle, int aIndex)
					{
						if (aIndex == 0)
						{
							stack.removeLast();
						}

						stack.removeLast();
					}
					@Override
					public Object process(Bundle aBundle, String aKey, Object aValue)
					{
//						stack.peekLast().add(new DefaultMutableTreeNode(aKey));
						return true;
					}
				}
			);

			JTree tree = new JTree(root, true);
			for (int i = 0; i < tree.getRowCount(); i++)
			{
				tree.expandRow(i);
			}

			DefaultTableModel dataModel = new DefaultTableModel(new Object[]{"Name","Type","Value"}, 0);

			JTable panel = new JTable(dataModel);

			tree.addTreeSelectionListener(new TreeSelectionListener()
			{
				@Override
				public void valueChanged(TreeSelectionEvent aEvent)
				{
					DefaultTableModel dataModel = new DefaultTableModel(new Object[]{"Name","Type","Value"}, 0);

					Object userObject = (((DefaultMutableTreeNode)aEvent.getPath().getLastPathComponent())).getUserObject();

					if (userObject instanceof TreeNode)
					{
						Bundle bundle = ((TreeNode)userObject).bundle;

						for (String key : bundle.keySet())
						{
							Object value = bundle.get(key);

							String type = value.getClass().getSimpleName();

							if (value instanceof Bundle || value instanceof Bundle[] || value instanceof Bundle[][] || (value instanceof ArrayList && ((ArrayList)value).size() > 0 && (((ArrayList)value).get(0) instanceof Bundle)))
							{
								value = "";
							}

							dataModel.addRow(new Object[]{key, type, value});
						}
					}

					panel.setModel(dataModel);

					panel.invalidate();
				}
			});

			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), new JScrollPane(panel));
			splitPane.setResizeWeight(0);

			JFrame frame = new JFrame();
			frame.add(splitPane);
			frame.setSize(1024, 768);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);

			splitPane.setDividerLocation(0.25);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}

	private static class TreeNode
	{
		Bundle bundle;
		String key;


		public TreeNode(Bundle aBundle, String aKey)
		{
			this.bundle = aBundle;
			this.key = aKey;
		}


		@Override
		public String toString()
		{
			return key == null ? "<root>" : key;
		}
	}
}
