namespace test {
	public class Test {
		protected void _Init() {
			{
				Tween.to(new Tween.Task(0, 2f) {
					public override void onEnd() {
						Camera.follow(Player.instance, false);
					}
				});
			}
		}


	}
}
