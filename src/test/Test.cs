using java.awt;

namespace test {
	public class BKState : State<BurningKnight>  {
		public override void update(float dt) {
			if self.target != null {
				self.lastSeen = new Point(self.target.x, self.target.y);
			} 

			base.update(dt);
		}
	}
}
