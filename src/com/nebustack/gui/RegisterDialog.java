package com.nebustack.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.nebustack.gui.preview.Preview;
import com.nebustack.gui.table.TableModel;
import com.nebustack.model.task.RegisterTask;
import com.nebustack.model.task.Scheduler;
import com.nebustack.model.task.TestRegisterTask;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class RegisterDialog extends JDialog {
	private final JPanel contentPanel = new JPanel();

	public RegisterDialog(JFrame parent, Scheduler scheduler, JTable table, TableModel model, Preview preview) {
		super(parent);

		setResizable(false);
		setTitle("Register");
		setLocationRelativeTo(parent);
		setSize(450, 270);

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		JLabel lblStarDetectionThreshold = new JLabel("Star detection threshold");
		JSlider thresholdSlider = new JSlider();
		thresholdSlider.setPaintTicks(true);
		thresholdSlider.setPaintLabels(true);
		thresholdSlider.setMajorTickSpacing(10);
		thresholdSlider.setMinorTickSpacing(5);
		JLabel lblMaximumStarSize = new JLabel("Maximum star radius");
		JSlider radiusSlider = new JSlider();
		radiusSlider.setMajorTickSpacing(4);
		radiusSlider.setMinorTickSpacing(1);
		radiusSlider.setPaintLabels(true);
		radiusSlider.setPaintTicks(true);
		radiusSlider.setValue(8);
		radiusSlider.setMaximum(16);
		radiusSlider.setMinimum(4);

		JLabel starsLabel = new JLabel(" ");

		final RegisterDialog registerDialog = this;

		JButton btnTestStarDetection = new JButton("Test star detection");
		btnTestStarDetection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabled(false);
				double v = thresholdSlider.getValue();
				scheduler.execute(new TestRegisterTask(parent, ((v * v) / 10000d) * 65535d, radiusSlider.getValue(), table, model, registerDialog, starsLabel));
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent e) {
				parent.setEnabled(true);
				parent.toFront();
				parent.repaint();
			}

			@Override
			public void windowOpened(WindowEvent e) {
				setLocationRelativeTo(parent);
				starsLabel.setText(" ");
				repaint();
			}
		});

		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)	
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblStarDetectionThreshold)
						.addComponent(thresholdSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblMaximumStarSize)
						.addComponent(radiusSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnTestStarDetection)
						.addComponent(starsLabel))
					.addContainerGap(224, Short.MAX_VALUE))
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblStarDetectionThreshold)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(thresholdSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblMaximumStarSize)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(radiusSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnTestStarDetection)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(starsLabel)
					.addContainerGap(21, Short.MAX_VALUE))
		);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
						parent.setEnabled(true);
						parent.toFront();

						double v = thresholdSlider.getValue();
						scheduler.execute(new RegisterTask(parent, ((v * v) / 10000d) * 65535d, radiusSlider.getValue(), model, preview));
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
						parent.setEnabled(true);
						parent.toFront();
					}
				});
				buttonPane.add(cancelButton);
			}
		}
	}
}
