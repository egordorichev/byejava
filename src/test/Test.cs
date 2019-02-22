using a;
using b;
using m.n;

namespace test {
	public class Test {
		protected void _Init() {
			{
				c = 32;
			}

			{
				d = 32;
			}
		}

		static Test() {
			a = 32;
		}

		public Test() {
			_Init();
		}

		public Test(int a) {
			_Init();
			this.a = a;
			thing.a = 32;
		}
	}
}
