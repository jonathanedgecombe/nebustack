package com.nebustack.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.nebustack.gui.table.TableModel;
import com.nebustack.model.stack.KappaSigmaStacker;
import com.nebustack.model.task.Scheduler;
import com.nebustack.model.task.StackTask;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class StackDialog extends JDialog {
	private final JPanel contentPanel = new JPanel();

	public StackDialog(JFrame parent, Scheduler scheduler, TableModel model) {
		super(parent);

		setResizable(false);
		setTitle("Stack");
		setLocationRelativeTo(parent);
		setSize(450, 380);

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
				repaint();
			}
		});

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JLabel lblDrizzle = new JLabel("Drizzle");
		
		JSlider slider = new JSlider();
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setMinorTickSpacing(1);
		slider.setMajorTickSpacing(1);
		slider.setValue(1);
		slider.setMaximum(4);
		slider.setMinimum(1);
		
		JLabel lblSupersampling = new JLabel("Supersampling");
		
		JSlider slider_1 = new JSlider();
		slider_1.setSnapToTicks(true);
		slider_1.setPaintLabels(true);
		slider_1.setPaintTicks(true);
		slider_1.setMinorTickSpacing(1);
		slider_1.setMajorTickSpacing(1);
		slider_1.setMaximum(4);
		slider_1.setMinimum(1);
		slider_1.setValue(1);
		
		JLabel lblRejectionThreshold = new JLabel("Rejection threshold");

		JTextField textField = new JTextField();
		textField.setText("2.0");
		textField.setColumns(10);
		
		JLabel lblRejectionIterations = new JLabel("Rejection iterations");
		
		JSlider slider_2 = new JSlider();
		slider_2.setSnapToTicks(true);
		slider_2.setPaintLabels(true);
		slider_2.setPaintTicks(true);
		slider_2.setValue(4);
		slider_2.setMinorTickSpacing(1);
		slider_2.setMajorTickSpacing(1);
		slider_2.setMinimum(1);
		slider_2.setMaximum(10);
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblDrizzle)
						.addComponent(slider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblSupersampling)
						.addComponent(slider_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblRejectionThreshold)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblRejectionIterations)
						.addComponent(slider_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(224, Short.MAX_VALUE))
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblDrizzle)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(slider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblSupersampling)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(slider_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblRejectionThreshold)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblRejectionIterations)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(slider_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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

						scheduler.execute(new StackTask(parent, model, new KappaSigmaStacker(1, 4, 2, 3)/*new MeanStacker(1, 2)*/));
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

		pack();
	}
}
