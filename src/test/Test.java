package test;

public class Test {
	{
		Tween.to(new Tween.Task(0, 2f) {
			@Override
			public void onEnd() {
				Camera.follow(Player.instance, false);
			}
		});
	}
}