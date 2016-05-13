package ru.blap.mayanwidget;

public class MayanWidgetSeconds extends MayanCalendarWidget {

	@SuppressWarnings("unused")
	private final String TAG = MayanWidgetSeconds.class.getSimpleName();

	public MayanWidgetSeconds() {
		super();
		UPDATE_INTERVAL = 1000;
		MAYANCALENDAR_WIDGET_UPDATE = "ru.blap.mayancalendar.action.WIDGET_UPDATE_SECONDS";
		LAYOUT = R.layout.mayancalendarseconds_layout;
	}

}
