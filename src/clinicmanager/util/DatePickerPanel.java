package clinicmanager.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Modern date picker panel with proper visual design like standard desktop applications
 */
public class DatePickerPanel extends JPanel {
    private Calendar calendar;
    private JLabel monthYearLabel;
    private JPanel daysPanel;
    private DateSelectionListener listener;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("MMMM yyyy");

    public DatePickerPanel(Date initialDate, DateSelectionListener listener) {
        this.listener = listener;
        this.calendar = Calendar.getInstance();
        if (initialDate != null) {
            calendar.setTime(initialDate);
        }
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(250, 250, 250));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCalendarPanel(), BorderLayout.CENTER);
        
        updateDisplay();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Left side: Navigation buttons and month/year
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        leftPanel.setBackground(new Color(245, 245, 245));

        JButton prevYearBtn = new JButton("<<");
        prevYearBtn.setFont(new Font("Arial", Font.BOLD, 12));
        prevYearBtn.setPreferredSize(new Dimension(40, 30));
        prevYearBtn.addActionListener(e -> {
            calendar.add(Calendar.YEAR, -1);
            updateDisplay();
        });
        leftPanel.add(prevYearBtn);

        JButton prevMonthBtn = new JButton("<");
        prevMonthBtn.setFont(new Font("Arial", Font.BOLD, 14));
        prevMonthBtn.setPreferredSize(new Dimension(40, 30));
        prevMonthBtn.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            updateDisplay();
        });
        leftPanel.add(prevMonthBtn);

        monthYearLabel = new JLabel();
        monthYearLabel.setFont(new Font("Arial", Font.BOLD, 14));
        monthYearLabel.setPreferredSize(new Dimension(140, 30));
        monthYearLabel.setHorizontalAlignment(JLabel.CENTER);
        monthYearLabel.setBackground(Color.WHITE);
        monthYearLabel.setOpaque(true);
        monthYearLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        leftPanel.add(monthYearLabel);

        JButton nextMonthBtn = new JButton(">");
        nextMonthBtn.setFont(new Font("Arial", Font.BOLD, 14));
        nextMonthBtn.setPreferredSize(new Dimension(40, 30));
        nextMonthBtn.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            updateDisplay();
        });
        leftPanel.add(nextMonthBtn);

        JButton nextYearBtn = new JButton(">>");
        nextYearBtn.setFont(new Font("Arial", Font.BOLD, 12));
        nextYearBtn.setPreferredSize(new Dimension(40, 30));
        nextYearBtn.addActionListener(e -> {
            calendar.add(Calendar.YEAR, 1);
            updateDisplay();
        });
        leftPanel.add(nextYearBtn);

        // Right side: Today button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightPanel.setBackground(new Color(245, 245, 245));

        JButton todayBtn = new JButton("Today");
        todayBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        todayBtn.setPreferredSize(new Dimension(80, 30));
        todayBtn.setBackground(new Color(66, 133, 244));
        todayBtn.setForeground(Color.WHITE);
        todayBtn.setFocusPainted(false);
        todayBtn.addActionListener(e -> {
            calendar.setTime(new Date());
            updateDisplay();
        });
        rightPanel.add(todayBtn);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createCalendarPanel() {
        daysPanel = new JPanel(new GridLayout(7, 7, 2, 2));
        daysPanel.setBackground(new Color(250, 250, 250));
        daysPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        return daysPanel;
    }

    private void updateDisplay() {
        monthYearLabel.setText(DISPLAY_FORMAT.format(calendar.getTime()));
        updateCalendarDays();
    }

    private void updateCalendarDays() {
        daysPanel.removeAll();

        // Add day headers
        String[] dayHeaders = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (String header : dayHeaders) {
            JLabel label = new JLabel(header, JLabel.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 11));
            label.setForeground(new Color(80, 80, 80));
            label.setBackground(new Color(230, 230, 230));
            label.setOpaque(true);
            label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 180)));
            daysPanel.add(label);
        }

        // Calculate first day and number of days
        Calendar tempCal = (Calendar) calendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Add empty cells before month starts
        for (int i = 0; i < firstDayOfWeek; i++) {
            JPanel emptyCell = new JPanel();
            emptyCell.setBackground(new Color(250, 250, 250));
            emptyCell.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            daysPanel.add(emptyCell);
        }

        // Add day buttons
        for (int day = 1; day <= daysInMonth; day++) {
            final int selectedDay = day;
            final int finalCurrentDay = currentDay;
            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFont(new Font("Arial", Font.PLAIN, 12));
            dayBtn.setFocusPainted(false);
            dayBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            dayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            if (day == currentDay) {
                // Current day - highlight in blue
                dayBtn.setBackground(new Color(66, 133, 244));
                dayBtn.setForeground(Color.WHITE);
                dayBtn.setFont(new Font("Arial", Font.BOLD, 12));
                dayBtn.setOpaque(true);
            } else {
                dayBtn.setBackground(Color.WHITE);
                dayBtn.setForeground(new Color(60, 60, 60));
                dayBtn.setOpaque(true);
            }

            dayBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (selectedDay != finalCurrentDay) {
                        dayBtn.setBackground(new Color(230, 240, 255));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (selectedDay != finalCurrentDay) {
                        dayBtn.setBackground(Color.WHITE);
                    }
                }
            });

            dayBtn.addActionListener(e -> {
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                if (listener != null) {
                    listener.dateSelected(DATE_FORMAT.format(calendar.getTime()));
                }
                updateDisplay();
            });
            
            daysPanel.add(dayBtn);
        }

        // Add empty cells after month ends
        int totalCells = 7 + firstDayOfWeek + daysInMonth;
        while (totalCells % 7 != 0) {
            JPanel emptyCell = new JPanel();
            emptyCell.setBackground(new Color(250, 250, 250));
            emptyCell.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            daysPanel.add(emptyCell);
            totalCells++;
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    public interface DateSelectionListener {
        void dateSelected(String date);
    }
}
