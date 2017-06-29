package com.nebustack.gui;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableColumnModel;

import com.nebustack.file.Frame;
import com.nebustack.file.FrameType;
import com.nebustack.gui.preview.Histogram;
import com.nebustack.gui.preview.Preview;
import com.nebustack.gui.table.SelectionListener;
import com.nebustack.gui.table.TableContextMenu;
import com.nebustack.gui.table.TableModel;
import com.nebustack.model.task.Scheduler;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import java.awt.Color;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowEvent;

public class Nebustack {
	private final static Dimension MINIMUM_SIZE = new Dimension(1280, 720);
	private final static Dimension MINIMUM_PANEL_SIZE = new Dimension(480, 360);
	private final static Dimension MINIMUM_TABLE_SIZE = new Dimension(480, 256);

	private final static int[] COLUMN_WIDTHS = {18, 48, 64, 128, 160, 128, 96, 96, 96, 96, 96, 96, 96};

	private final JFrame frame;

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Nebustack window = new Nebustack();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Nebustack() {
		this.frame = new JFrame();
		initialize();
	}

	private void initialize() {
		frame.setMinimumSize(MINIMUM_SIZE);
		frame.setPreferredSize(MINIMUM_SIZE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Nebustack");

		TableModel model = new TableModel();
		JTable table = new JTable(model);

		ProgressDialog progressDialog = new ProgressDialog(frame);
		Scheduler scheduler = new Scheduler(frame, progressDialog);

		Histogram histogram = new Histogram();
		histogram.setSize(200, 64);
		Preview preview = new Preview(histogram);

		RegisterDialog registerDialog = new RegisterDialog(frame, scheduler, table, model, preview);
		StackDialog stackDialog = new StackDialog(frame, scheduler, model);

		frame.addWindowFocusListener(new WindowFocusListener() {
			public void windowGainedFocus(WindowEvent e) {
				if (progressDialog.isVisible()) progressDialog.toFront();
				else if (registerDialog.isVisible()) registerDialog.toFront();
				else if (stackDialog.isVisible()) stackDialog.toFront();
			}

			public void windowLostFocus(WindowEvent e) {}
		});

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.7);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);

		JSplitPane previewGroup = new JSplitPane();
		previewGroup.setResizeWeight(1);
		previewGroup.setMinimumSize(MINIMUM_PANEL_SIZE);

		splitPane.setLeftComponent(previewGroup);

		JScrollPane tableScroll = new JScrollPane();
		tableScroll.setBackground(Color.WHITE);
		tableScroll.getViewport().setBackground(Color.WHITE);

		table.getSelectionModel().addListSelectionListener(new SelectionListener(model, preview, table));

		table.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseReleased(MouseEvent e) {
		    	if (!SwingUtilities.isRightMouseButton(e)) return;

				int r = table.rowAtPoint(e.getPoint());
				if (r >= 0 && r < table.getRowCount()) {
					boolean isSelected = false;
					for (int index : table.getSelectedRows()) {
						if (index == r) isSelected = true;
					}

					if (!isSelected) table.setRowSelectionInterval(r, r);
				} else {
					table.clearSelection();
				}

				int rowindex = table.getSelectedRow();
				if (rowindex < 0)
					return;
				if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
					TableContextMenu popup = new TableContextMenu(table.getSelectedRowCount() == 1, table, model);
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
		    }
		});

		Color borderColor = table.getTableHeader().getBackground();
		table.setBackground(Color.WHITE);
		table.getTableHeader().setBackground(Color.WHITE);
		table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));
		table.getTableHeader().setReorderingAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		table.getColumnModel().getColumn(0).setMaxWidth(18);

		TableColumnModel columnModel = table.getColumnModel();
		for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
			columnModel.getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
		}

		table.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyReleased(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {
				int row = table.getSelectedRow();
				if (row < 0) return;

				Frame frame = model.get(row);

				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					frame.setChecked(!frame.isChecked());
					model.fireTableCellUpdated(row, 0);
				} else if (e.getKeyChar() == KeyEvent.VK_R) {
					model.setReference(frame);
					model.fireTableCellUpdated(row, 3);
				}
			}
		});

		tableScroll.setPreferredSize(MINIMUM_TABLE_SIZE);
		tableScroll.setMinimumSize(MINIMUM_TABLE_SIZE);
		table.setFillsViewportHeight(true);

		tableScroll.setViewportView(table);
		splitPane.setRightComponent(tableScroll);

		JPanel detailsPanel = new JPanel();
		JScrollPane detailsScrollPane = new JScrollPane(detailsPanel);
		detailsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		previewGroup.setRightComponent(detailsScrollPane);
		detailsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		detailsScrollPane.setMinimumSize(new Dimension(216, 1));

		JLabel lblContrast = new JLabel("Contrast");
		JSlider contrastSlider = new JSlider(1, 100, 1);

		JLabel lblBrightness = new JLabel("Brightness");
		JSlider brightnessSlider = new JSlider(1, 100, 50);

		ChangeListener sliderListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				preview.setScaleOffset(contrastSlider.getValue(), brightnessSlider.getValue());
			}
		};

		contrastSlider.addChangeListener(sliderListener);
		brightnessSlider.addChangeListener(sliderListener);

		JLabel lblHistogram = new JLabel("Histogram");
		
		JLabel lblFileName = new JLabel("File Name");
		JLabel lblPath = new JLabel("Path");
		JLabel lblDatetime = new JLabel("Date/Time");
		
		JLabel lblCamera = new JLabel("Camera");
		JLabel lblResolution = new JLabel("Resolution");
		JLabel lblColorformat = new JLabel("ColorFormat");
		JLabel lblGain = new JLabel("Gain");
		JLabel lblExposure = new JLabel("Exposure");
		
		JLabel lblStars = new JLabel("Stars");
		JLabel lblFwhm = new JLabel("FWHM");
		JLabel lblBackgroundLevel = new JLabel("Background Level");

		GroupLayout gl_detailsPanel = new GroupLayout(detailsPanel);
		gl_detailsPanel.setHorizontalGroup(
			gl_detailsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_detailsPanel.createSequentialGroup()
					.addGroup(gl_detailsPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblContrast)
						.addComponent(contrastSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblBrightness)
						.addComponent(brightnessSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblHistogram)
						.addComponent(histogram, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblFileName)
						.addComponent(lblPath)
						.addComponent(lblDatetime)
						.addComponent(lblCamera)
						.addGroup(gl_detailsPanel.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblResolution))
						.addGroup(gl_detailsPanel.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblColorformat))
						.addGroup(gl_detailsPanel.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblGain))
						.addGroup(gl_detailsPanel.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblExposure))
						.addComponent(lblStars)
						.addComponent(lblFwhm)
						.addComponent(lblBackgroundLevel))
					.addContainerGap(20, Short.MAX_VALUE))
		);
		gl_detailsPanel.setVerticalGroup(
			gl_detailsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_detailsPanel.createSequentialGroup()
					.addComponent(lblContrast)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(contrastSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblBrightness)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(brightnessSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(lblHistogram)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(histogram, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(lblFileName)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblPath)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblDatetime)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblCamera)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblResolution)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblColorformat)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblGain)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblExposure)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblStars)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblFwhm)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblBackgroundLevel)
					.addContainerGap(1929, Short.MAX_VALUE))
		);
		detailsPanel.setLayout(gl_detailsPanel);

		preview.setMinimumSize(MINIMUM_PANEL_SIZE);
		previewGroup.setLeftComponent(preview);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmOpenLightFrames = new JMenuItem("Open light frames...");
		mntmOpenLightFrames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FilePicker.open(table, model, frame, scheduler, FrameType.LIGHT);
			}
		});
		mnFile.add(mntmOpenLightFrames);

		JMenuItem mntmOpenDarkFrames = new JMenuItem("Open dark frames...");
		mntmOpenDarkFrames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FilePicker.open(table, model, frame, scheduler, FrameType.DARK);
			}
		});
		mnFile.add(mntmOpenDarkFrames);

		JMenuItem mntmOpenBiasFrames = new JMenuItem("Open bias frames...");
		mntmOpenBiasFrames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FilePicker.open(table, model, frame, scheduler, FrameType.BIAS);
			}
		});
		mnFile.add(mntmOpenBiasFrames);

		JMenuItem mntmOpenFlatFrames = new JMenuItem("Open flat frames...");
		mntmOpenFlatFrames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FilePicker.open(table, model, frame, scheduler, FrameType.FLAT);
			}
		});
		mnFile.add(mntmOpenFlatFrames);

		JMenuItem mntmOpenDarkFlat = new JMenuItem("Open dark flat frames...");
		mntmOpenDarkFlat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FilePicker.open(table, model, frame, scheduler, FrameType.DARK_FLAT);
			}
		});
		mnFile.add(mntmOpenDarkFlat);

		mnFile.addSeparator();

		JMenuItem mntmSaveFrame = new JMenuItem("Save frame as...");
		mnFile.add(mntmSaveFrame);

		JMenu mnProcessing = new JMenu("Processing");
		menuBar.add(mnProcessing);

		JMenuItem mntmRegister = new JMenuItem("Register");
		mntmRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setEnabled(false);
				registerDialog.setVisible(true);
			}
		});
		mnProcessing.add(mntmRegister);

		JMenuItem mntmStack = new JMenuItem("Stack");
		mntmStack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setEnabled(false);
				stackDialog.setVisible(true);
			}
		});
		mnProcessing.add(mntmStack);

		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);

		JRadioButtonMenuItem rdbtnmntmFrames = new JRadioButtonMenuItem("Frames");
		rdbtnmntmFrames.setSelected(true);
		mnView.add(rdbtnmntmFrames);

		JRadioButtonMenuItem rdbtnmntmStackedImage = new JRadioButtonMenuItem("Stacked image");
		mnView.add(rdbtnmntmStackedImage);

		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnmntmFrames);
		group.add(rdbtnmntmStackedImage);

		JMenu mnOptions = new JMenu("Options");
		menuBar.add(mnOptions);

		JMenuItem mntmSettings = new JMenuItem("Settings...");
		mnOptions.add(mntmSettings);

		frame.pack();
	}
}
