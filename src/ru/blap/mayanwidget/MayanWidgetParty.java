package ru.blap.mayanwidget;

public class MayanWidgetParty extends MayanCalendarWidget {

	@SuppressWarnings("unused")
	private final String TAG = MayanWidgetParty.class.getSimpleName();

	public MayanWidgetParty() {
		super();
		AFTER_TEXT = R.string.after_party;
		BEFORE_TEXT = R.string.before_party;
		MAYANCALENDAR_WIDGET_UPDATE = "ru.blap.mayancalendar.action.WIDGET_UPDATE_PARTY";
	}

}
