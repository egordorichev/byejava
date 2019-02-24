using box2dLight;
using com.badlogic.gdx;
using com.badlogic.gdx.graphics;
using com.badlogic.gdx.graphics.g2d;
using com.badlogic.gdx.graphics.glutils;
using com.badlogic.gdx.math;
using com.badlogic.gdx.physics.box2d;
using com.badlogic.gdx.utils;
using org.rexcellentgames.burningknight;
using org.rexcellentgames.burningknight.assets;
using org.rexcellentgames.burningknight.entity;
using org.rexcellentgames.burningknight.entity.creature;
using org.rexcellentgames.burningknight.entity.creature.buff;
using org.rexcellentgames.burningknight.entity.creature.fx;
using org.rexcellentgames.burningknight.entity.creature.inventory;
using org.rexcellentgames.burningknight.entity.creature.mob;
using org.rexcellentgames.burningknight.entity.creature.mob.boss;
using org.rexcellentgames.burningknight.entity.creature.mob.tech;
using org.rexcellentgames.burningknight.entity.creature.player.fx;
using org.rexcellentgames.burningknight.entity.fx;
using org.rexcellentgames.burningknight.entity.item;
using org.rexcellentgames.burningknight.entity.item.accessory;
using org.rexcellentgames.burningknight.entity.item.accessory.hat;
using org.rexcellentgames.burningknight.entity.item.active;
using org.rexcellentgames.burningknight.entity.item.autouse;
using org.rexcellentgames.burningknight.entity.item.consumable;
using org.rexcellentgames.burningknight.entity.item.entity;
using org.rexcellentgames.burningknight.entity.item.key;
using org.rexcellentgames.burningknight.entity.item.weapon;
using org.rexcellentgames.burningknight.entity.item.weapon.gun;
using org.rexcellentgames.burningknight.entity.item.weapon.sword;
using org.rexcellentgames.burningknight.entity.level;
using org.rexcellentgames.burningknight.entity.level.entities;
using org.rexcellentgames.burningknight.entity.level.entities.fx;
using org.rexcellentgames.burningknight.entity.level.rooms;
using org.rexcellentgames.burningknight.entity.level.rooms.boss;
using org.rexcellentgames.burningknight.entity.level.rooms.shop;
using org.rexcellentgames.burningknight.entity.level.save;
using org.rexcellentgames.burningknight.game;
using org.rexcellentgames.burningknight.game.input;
using org.rexcellentgames.burningknight.game.state;
using org.rexcellentgames.burningknight.physics;
using org.rexcellentgames.burningknight.ui;
using org.rexcellentgames.burningknight.util;
using org.rexcellentgames.burningknight.util.file;
using java.io;
using system.collections.generic;

namespace org.rexcellentgames.burningknight.entity.creature.player {
	public class Player : Creature {
		protected void _Init() {
			{
				Defense = 1;
			}

			{
				Hpmax = 6;
				Manamax = 8;
				Level = 1;
				Mul = 0.7f;
				Speed = 25;
				Alwaysactive = true;
				Invtime = 1f;
				Setskin("body");
			}
		}

		static Player() {
			Shader = new Shaderprogram(Gdx.Files.Internal("shaders/default.vert").Readstring(), Gdx.Files.Internal("shaders/rainbow.frag").Readstring());

			if (!Shader.Iscompiled()) throw new Gdxruntimeexception("Couldn't compile shader: " + Shader.Getlog());

		}

		enum Type {
			Warrior,
			Wizard,
			Ranger,
			None
		}

		public static Type Toset = Type.None;
		public static Item Startingitem;
		public static float Mobspawnmodifier = 1f;
		public static List<Player> All = new List<>();
		public static Player Instance;
		public static Entity Ladder;
		public static Shaderprogram Shader;
		public static bool Showstats;
		public static string Hatid;
		public static string Skin;
		public static Textureregion Balloon = Graphics.Gettexture("item-red_balloon");
		public static bool Sucked = false;
		public static bool Dulldamage;
		private static Dictionary<string, Animation> Skins = new Dictionary<>();
		private static Animation Headanimations = Animation.Make("actor-gobbo", "-gobbo");
		private static Animationdata Headidle = Headanimations.Get("idle");
		private static Animationdata Headrun = Headanimations.Get("run");
		private static Animationdata Headhurt = Headanimations.Get("hurt");
		private static Animationdata Headroll = Headanimations.Get("roll");
		private static Textureregion Wing = Graphics.Gettexture("item-half_wing");
		private static Textureregion Playertexture = Graphics.Gettexture("props-gobbo_full");
		private static int[] Offsets = { 0, 0, 0, -1, -1, -1, 0, 0, 1, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		public Type Type;
		public float Heat;
		public bool Hasredline;
		public Uiinventory Ui;
		public List<Uibuff> Uibuffs = new List<>();
		public float Goldmodifier = 1f;
		public int Lavaresist;
		public int Fireresist;
		public int Sales;
		public int Poisonresist;
		public int Stunresist;
		public bool Seesecrets;
		public bool Todeath;
		public bool Drawinvt;
		public Itempickupfx Pickupfx;
		public float Accuracy;
		public bool Seepath;
		public float Stopt;
		public bool Rotating;
		public float Al;
		public bool Hasbkkey;
		public bool Leavesmall;
		public Vector2 Orbitalring = new Vector2();
		public int Step;
		public float Tt;
		public int Frostlevel;
		public int Flight;
		public int Numcollectedhearts;
		public int Burnlevel;
		public int Leavevenom;
		protected float Mana;
		protected int Manamax;
		protected int Level;
		private Inventory Inventory;
		private string Name;
		private float Fa;
		private float Sx = 1f;
		private float Sy = 1f;
		private List<Itemholder> Holders = new List<>();
		private Animationdata Idle;
		private Animationdata Run;
		private Animationdata Hurt;
		private Animationdata Roll;
		private Animationdata Killed;
		private Animationdata Animation;
		private Pointlight Light;
		private int Money;
		private int Bombs;
		private int Keys;
		private Textureregion Hat;
		private int Numironhearts;
		private int Numgoldenhearts;
		private bool Wasfreezed;
		private bool Waspoisoned;
		private bool Gothit;
		private float Last;
		private float Lastblood;
		private bool Teleport;
		private float Zvel;
		private bool Onground;
		private Vector2 Lastground = new Vector2();
		private bool Moved;
		private bool Rolled;
		private float Lastfx = 0;
		private bool Rolling;
		private bool Hadenemies;

		public static float Getstaticmage() {
			return Instance == null ? (Toset == Type.Wizard ? 1f : 0.1f) : Instance.Getmage();
		}

		public static float Getstaticwarrior() {
			return Instance == null ? (Toset == Type.Warrior ? 1f : 0.1f) : Instance.Getwarrior();
		}

		public static float Getstaticranger() {
			return Instance == null ? (Toset == Type.Ranger ? 1f : 0.1f) : Instance.Getranger();
		}

		public override Void Renderbuffs() {
			base.Renderbuffs();
			Graphics.Batch.Setprojectionmatrix(Camera.Game.Combined);
			Graphics.Batch.Setprojectionmatrix(Camera.Game.Combined);
			Item Item = this.Inventory.Getslot(this.Inventory.Active);

			if (Item is Gun) {
				((Gun) Item).Renderreload();
			} 

			if (Burningknight.Instance != null && (Burningknight.Instance.Rage) && Exit.Instance != null) {
				float Dx = Exit.Instance.X + 8 - X - W / 2;
				float Dy = Exit.Instance.Y + 8 - Y - H / 2;
				float A = (float) Math.Atan2(Dy, Dx);
				Graphics.Batch.End();
				Graphics.Shape.Setprojectionmatrix(Camera.Game.Combined);
				Graphics.Shape.Begin(Shaperenderer.Shapetype.Filled);
				float An = (float) Math.Toradians(10);
				float D = (float) (28 + Math.Cos(Dungeon.Time * 6) * 2.5f);
				float D2 = D + 8;
				Graphics.Shape.Setcolor(0, 0, 0, 1);
				Graphics.Shape.Rectline((float) (X + W / 2 + Math.Cos(A - An) * D), (float) (Y + H / 2 + Math.Sin(A - An) * D), (float) (X + W / 2 + Math.Cos(A) * D2), (float) (Y + H / 2 + Math.Sin(A) * D2), 4f);
				Graphics.Shape.Rectline((float) (X + W / 2 + Math.Cos(A + An) * D), (float) (Y + H / 2 + Math.Sin(A + An) * D), (float) (X + W / 2 + Math.Cos(A) * D2), (float) (Y + H / 2 + Math.Sin(A) * D2), 4f);
				float V = (float) (Math.Sin(Dungeon.Time * 12) * 0.5f + 0.5f);
				Graphics.Shape.Setcolor(1, V, V, 1);
				Graphics.Shape.Rectline((float) (X + W / 2 + Math.Cos(A - An) * D), (float) (Y + H / 2 + Math.Sin(A - An) * D), (float) (X + W / 2 + Math.Cos(A) * D2), (float) (Y + H / 2 + Math.Sin(A) * D2), 2);
				Graphics.Shape.Rectline((float) (X + W / 2 + Math.Cos(A + An) * D), (float) (Y + H / 2 + Math.Sin(A + An) * D), (float) (X + W / 2 + Math.Cos(A) * D2), (float) (Y + H / 2 + Math.Sin(A) * D2), 2);
				Graphics.Shape.End();
				Graphics.Batch.Begin();
			} 

			if (Dungeon.Depth < 0) {
				return;
			} 

			int Count = 0;
			Mob Last = null;

			foreach (Mob Mob in Mob.All) {
				if (Mob.Room == this.Room) {
					Last = Mob;
					Count++;
				} 
			}

			if (Last != null && Count == 1 && !Ui.Hideui) {
				float Dx = Last.X + Last.W / 2 - this.X - this.W / 2;
				float Dy = Last.Y + Last.H / 2 - this.Y - this.H / 2;
				float D = (float) Math.Sqrt(Dx * Dx + Dy * Dy);
				float A = this.Getangleto(Last.X + Last.W / 2, Last.Y + Last.H / 2);

				if (D < 48) {
					return;
				} 

				D -= 32;
				float Cx = Camera.Game.Position.X;
				float Cy = Camera.Game.Position.Y;
				float X = Mathutils.Clamp(Cx - Display.Game_width / 2 + 16, Cx + Display.Game_width / 2 - 16, (float) Math.Cos(A) * D + this.X + this.W / 2);
				float Y = Mathutils.Clamp(Cy - Display.Game_height / 2 + 16, Cy + Display.Game_height / 2 - 16, (float) Math.Sin(A) * D + this.Y + this.H / 2);
				Graphics.Startalphashape();
				Graphics.Shape.Setprojectionmatrix(Camera.Game.Combined);
				Graphics.Shape.Setcolor(1, 0.1f, 0.1f, 0.8f);
				A = (float) Math.Atan2(Y - Last.Y - Last.W / 2, X - Last.X - Last.H / 2);
				float M = 10;
				float Am = 0.5f;
				Graphics.Shape.Rectline(X, Y, X + (float) Math.Cos(A - Am) * M, Y + (float) Math.Sin(A - Am) * M, 2);
				Graphics.Shape.Rectline(X, Y, X + (float) Math.Cos(A + Am) * M, Y + (float) Math.Sin(A + Am) * M, 2);
				Graphics.Endalphashape();
			} 
		}

		public Player() {
			_Init();
			this("player");
		}

		public Player(string Name) {
			_Init();
			if (Player.Instance != null) {
				Player.Instance.Done = true;
				Player.Instance.Destroy();
			} 

			All.Add(this);
			Instance = this;
			Ui.Ui.Dead = false;
		}

		public float Getmage() {
			return this.Type == Type.Wizard ? 1f : 0.1f;
		}

		public float Getwarrior() {
			return this.Type == Type.Warrior ? 1f : 0.1f;
		}

		public override Void Sethpmax(int Hpmax) {
			base.Sethpmax(Hpmax);

			if (this.Hpmax >= 16) {
				Achievements.Unlock(Achievements.Get_8_heart_containers);
			} 
		}

		public float Getranger() {
			return this.Type == Type.Ranger ? 1f : 0.1f;
		}

		public int Getkeys() {
			return Keys;
		}

		public Void Setkeys(int Money) {
			this.Keys = Math.Min(99, Money);
		}

		public Void Resethit() {
			Gothit = false;
		}

		public int Getbombs() {
			return Bombs;
		}

		public Void Setbombs(int Money) {
			this.Bombs = Math.Min(99, Money);
		}

		public int Getmoney() {
			return Money;
		}

		public Void Setmoney(int Money) {
			this.Money = Money;

			if (Money >= 100) {
				Achievements.Unlock(Achievements.Unlock_money_printer);
			} 

			if (Money >= 300) {
				Achievements.Unlock(Achievements.Collect_300_gold);
			} 
		}

		public Type Gettype() {
			return this.Type;
		}

		public Void Settype(Type Type) {
			this.Type = Type;
		}

		public override Void Rendershadow() {
			float Z = this.Z;
			bool Flying = false;
			Graphics.Shadow(this.X + this.Hx, this.Y - (Flying ? 3 : 0), this.Hw, this.Hh, Z);
		}

		public Void Setskin(string Add) {
			Animation Animations;
			Skin = Add;

			if (!Add.Isempty()) {
				Add = "-" + Add;
			} 

			if (Skins.Containskey(Add)) {
				Animations = Skins.Get(Add);
			} else {
				Animations = Animation.Make("actor-gobbo", Add);
				Skins.Put(Add, Animations);
			}


			Idle = Animations.Get("idle");
			Run = Animations.Get("run");
			Hurt = Animations.Get("hurt");
			Roll = Animations.Get("roll");
			Killed = Animations.Get("dead");
			Animation = this.Idle;
		}

		public override Void Destroy() {
			base.Destroy();
			World.Removelight(Light);

			if (Uimap.Instance != null) {
				Uimap.Instance.Remove();
			} 

			Hasbkkey = false;
			Player.All.Remove(this);
		}

		public string Getname() {
			return this.Name;
		}

		public Void Setname(string Name) {
			this.Name = Name;
		}

		public override Void Init() {
			base.Init();
			Invt = 0.5f;
			Al = 0;
			Rotating = false;
			Tween.To(new Tween.Task(0, 0.1f) {
				public override Void Onend() {
					base.Onend();
					Camera.Follow(Player.Instance, true);
				}
			});
			T = 0;

			if (Toset != Type.None) {
				this.Type = Toset;
				Toset = Type.None;
			} else if (this.Type == null) {
				this.Type = Type.Warrior;
			} 

			if (Instance == null) {
				Instance = this;
			} 

			this.Mana = this.Manamax;
			this.Inventory = new Inventory(this);
			this.Body = this.Createsimplebody(3, 0, 10, 11, Bodydef.Bodytype.Dynamicbody, false);
			Dotp(true);

			switch (this.Type) {
				case Warrior: 
				case Wizard: {
					this.Accuracy -= 5;

					break;
				}
			}

			Light = World.Newlight(256, new Color(1, 1, 1, 1f), 180, X, Y);
			Light.Setposition(this.X + 8, this.Y + 8);
			Light.Attachtobody(this.Body, 8, 8, 0);
			Light.Setignoreattachedbody(true);

			if (Dungeon.Depth == -3) {
				this.Inventory.Clear();
				this.Hpmax = 10;
				this.Hp = 10;
				this.Give(new Sword());
				Player.Instance.Tp(Spawn.Instance.X, Spawn.Instance.Y);
			} 

			Camera.Follow(this, true);
		}

		public Void Generate() {
			this.Inventory.Clear();
			Bombs = 1;
			Keys = 0;
			Money = 0;

			if (Dungeon.Depth == -3) {
				this.Hpmax = 12;
				this.Hp = 12;
				this.Give(new Sword());
			} else {
				if (Startingitem != null) {
					this.Give(Startingitem);
					Startingitem = null;
				} else {
					this.Give(new Sword());
				}


				if (this.Type != Type.Wizard) {
					this.Manamax -= 2;
					this.Mana -= 2;
				} 

				if (this.Type == Type.Ranger) {
					this.Hpmax = 4;
					this.Hp = 4;
				} 
			}


			if (Hatid != null) {
				this.Give(Itempickupfx.Setskin(Hatid));
			} else {
				string Id = Globalsave.Getstring("last_hat", null);
				this.Sethat(Id);
			}


			if (Random.Getseed().Equals("HP")) {
				this.Hpmax = 12;
				this.Hp = 12;
			} else if (Random.Getseed().Equals("DIE")) {
				this.Hpmax = 2;
				this.Hp = 2;
			} else if (Random.Getseed().Equals("BOMB")) {
				this.Bombs = 99;
			} else if (Random.Getseed().Equals("KEY")) {
				this.Keys = 99;
			} else if (Random.Getseed().Equals("GOLD")) {
				this.Money = 999;
			} 
		}

		public Void Give(Item Item) {
			if (Item is Hat) {
				this.Inventory.Setslot(3, Item);
				Item.Setowner(this);
				((Accessory) Item).Onequip(false);
			} else {
				this.Inventory.Add(new Itemholder(Item));
			}

		}

		public Void Sethat(string Name) {
			if (Name == null || Name.Isempty()) {
				Hat = null;

				return;
			} 

			Globalsave.Put("last_hat", Name);
			Hatid = Name;
			this.Hat = Graphics.Gettexture("hat-" + Name + "-idle-00");
		}

		public override Void Tp(float X, float Y) {
			base.Tp(X, Y);
			Camera.Follow(this, true);
			Orbitalring.X = this.X + this.W / 2;
			Orbitalring.Y = this.Y + this.H / 2;
		}

		public Void Addironhearts(int A) {
			Numironhearts += A;
		}

		public Void Addgoldenhearts(int A) {
			Numgoldenhearts += A;
		}

		public int Getironhearts() {
			return Numironhearts;
		}

		public int Getgoldenhearts() {
			return Numgoldenhearts;
		}

		public Void Setui(Uiinventory Ui) {
			this.Ui = Ui;
		}

		public override bool Isunhittable() {
			return base.Isunhittable() || this.Rolling;
		}

		public Void Modifymanamax(int A) {
			this.Manamax += A;
			this.Modifymana(0);
		}

		public Void Modifymana(int A) {
			this.Mana = (int) Mathutils.Clamp(0, this.Manamax, this.Mana + A);
		}

		public override Void Render() {
			Graphics.Batch.Setcolor(1, 1, 1, this.A);
			float Offset = 0;

			if (this.Rotating) {
				this.Al += Gdx.Graphics.Getdeltatime() * 960;
				Graphics.Render(Playertexture, this.X + 6.5f, this.Y + 2.5f, this.Al, 6.5f, 2.5f, false, false);
				Graphics.Batch.Setcolor(1, 1, 1, 1);
			} else {
				if (this.Rolling) {
					this.Animation = Roll;
				} else if (this.Invt > 0) {
					this.Animation = Hurt;
					Hurt.Setframe(0);
				} else if (!this.Isflying() && this.State.Equals("run")) {
					this.Animation = Run;
				} else {
					this.Animation = Idle;
				}


				if (this.Invtt == 0) {
					this.Drawinvt = false;
				} 

				int Id = this.Animation.Getframe();
				float Of = Offsets[Id] - 2;

				if (this.Invt > 0) {
					Id += 16;
				} else if (!this.Isflying() && this.State.Equals("run")) {
					Id += 8;
				} 

				if (this.Ui != null && !Isrolling()) {
					this.Ui.Renderbeforeplayer(this, Of);
				} 

				bool Shade = (this.Drawinvt && this.Invtt > 0) || (Invt > 0 && Invt % 0.2f > 0.1f);
				Textureregion Region = this.Animation.Getcurrent().Frame;

				if (Shade) {
					Texture Texture = Region.Gettexture();
					Graphics.Batch.End();
					Shader.Begin();
					Shader.Setuniformf("time", Dungeon.Time);
					Shader.Setuniformf("pos", new Vector2((float) Region.Getregionx() / Texture.Getwidth(), (float) Region.Getregiony() / Texture.Getheight()));
					Shader.Setuniformf("size", new Vector2((float) Region.Getregionwidth() / Texture.Getwidth(), (float) Region.Getregionheight() / Texture.Getheight()));
					Shader.Setuniformf("a", this.A);
					Shader.Setuniformf("white", Invt > 0 ? 1 : 0);
					Shader.End();
					Graphics.Batch.Setshader(Shader);
					Graphics.Batch.Begin();
				} else if (this.Fa > 0) {
					Graphics.Batch.End();
					Mob.Frozen.Begin();
					Mob.Frozen.Setuniformf("time", Dungeon.Time);
					Mob.Frozen.Setuniformf("f", this.Fa);
					Mob.Frozen.Setuniformf("a", this.A);
					Mob.Frozen.Setuniformf("freezed", this.Wasfreezed ? 1f : 0f);
					Mob.Frozen.Setuniformf("poisoned", this.Waspoisoned ? 1f : 0f);
					Mob.Frozen.End();
					Graphics.Batch.Setshader(Mob.Frozen);
					Graphics.Batch.Begin();
				} 

				if (this.Freezed || this.Poisoned) {
					this.Fa += (1 - this.Fa) * Gdx.Graphics.Getdeltatime() * 3f;
					this.Wasfreezed = this.Freezed;
					this.Waspoisoned = this.Poisoned;
				} else {
					this.Fa += (0 - this.Fa) * Gdx.Graphics.Getdeltatime() * 3f;

					if (this.Fa <= 0) {
						this.Wasfreezed = false;
						this.Waspoisoned = false;
					} 
				}


				float Angle = 0;
				this.Animation.Render(this.X - Region.Getregionwidth() / 2 + 8, this.Y + this.Z + Offset, false, false, Region.Getregionwidth() / 2, 0, Angle, this.Sx * (this.Flipped ? -1 : 1), this.Sy);

				if (this.Hat != null && !this.Isrolling()) {
					Graphics.Render(this.Hat, this.X + W / 2 - (this.Flipped ? -1 : 1) * 7, this.Y + 1 + this.Z + Offsets[Id] + Region.Getregionheight() / 2 - 2 + Offset, Angle, Region.Getregionwidth() / 2, 0, false, false, this.Sx * (this.Flipped ? -1 : 1), this.Sy);
				} else {
					Animationdata Anim = Headidle;

					if (this.Rolling) {
						Anim = Headroll;
					} else if (this.Invt > 0) {
						Anim = Headhurt;
					} else if (this.State.Equals("run") && !this.Isflying()) {
						Anim = Headrun;
					} 

					Anim.Setframe(this.Animation.Getframe());
					Region = Anim.Getcurrent().Frame;
					Anim.Render(this.X - Region.Getregionwidth() / 2 + 8, this.Y + this.Z + Offset, false, false, Region.Getregionwidth() / 2, 0, Angle, this.Sx * (this.Flipped ? -1 : 1), this.Sy);
				}


				Graphics.Batch.Setcolor(1, 1, 1, 1);

				if (Shade || this.Fa > 0) {
					Graphics.Batch.End();
					Graphics.Batch.Setshader(null);
					Graphics.Batch.Begin();
				} 

				if (!this.Rolling && this.Ui != null && Dungeon.Depth != -2) {
					this.Ui.Renderonplayer(this, Of + Offset);
				} 
			}


			Graphics.Batch.Setcolor(1, 1, 1, 1);
		}

		public bool Isrolling() {
			return this.Rolling;
		}

		public override Void Oncollision(Entity Entity) {
			if (Entity is Itemholder) {
				Itemholder Item = (Itemholder) Entity;

				if (Item.Getitem() is Coin) {
					Item.Remove();
					Item.Done = true;
					Tween.To(new Tween.Task(20, 0.2f, Tween.Type.Back_out) {
						public override float Getvalue() {
							return Ui.Y;
						}

						public override Void Setvalue(float Value) {
							Ui.Y = Value;
						}

						public override Void Onend() {
							Tween.To(new Tween.Task(0, 0.1f) {
								public override Void Onend() {
									Globalsave.Put("num_coins", Globalsave.Getint("num_coins") + 1);
								}
							}).Delay(0.5f);
							Tween.To(new Tween.Task(0, 0.2f) {
								public override float Getvalue() {
									return Ui.Y;
								}

								public override Void Setvalue(float Value) {
									Ui.Y = Value;
								}

								public override Void Onstart() {
									if (Ui.Y < 20) {
										Deleteself();
									} 
								}
							}).Delay(3.1f);
						}
					});

					for (int I = 0; I < 10; I++) {
						Pooffx Fx = new Pooffx();
						Fx.X = Item.X + Item.W / 2;
						Fx.Y = Item.Y + Item.H / 2;
						Dungeon.Area.Add(Fx);
					}
				} else if (!Item.Getitem().Shop && (Item.Getitem().Hasautopickup() || Item.Getauto())) {
					if (this.Trytopickup(Item) && !Item.Getauto()) {
						if (!(Item.Getitem() is Gold)) {
							this.Area.Add(new Itempickedfx(Item));
						} 

						Item.Done = true;
						Item.Remove();
					} 
				} else if (!Item.Getfalling()) {
					if (Item is Classselector) {
						if (((Classselector) Item).Same(this.Type)) {
							return;
						} 
					} 

					this.Holders.Add(Item);

					if (this.Pickupfx == null) {
						this.Pickupfx = new Itempickupfx(Item, this);
						this.Area.Add(this.Pickupfx);
					} 
				} 
			} else if (Entity is Mob) {
				if (this.Frostlevel > 0) {
					((Mob) Entity).Addbuff(new Freezebuff());
				} 

				if (this.Burnlevel > 0) {
					((Mob) Entity).Addbuff(new Burningbuff());
				} 
			} 
		}

		public override Void Oncollisionend(Entity Entity) {
			if (Entity is Itemholder) {
				if (this.Pickupfx != null) {
					this.Pickupfx.Remove();
					this.Pickupfx = null;
				} 

				this.Holders.Remove(Entity);

				if (this.Holders.Size() > 0 && !Ui.Hideui) {
					this.Pickupfx = new Itempickupfx(this.Holders.Get(0), this);
					this.Area.Add(this.Pickupfx);
				} 
			} 
		}

		public bool Trytopickup(Itemholder Item) {
			if (!Item.Done) {
				if (Item.Getitem() is Bomb && !(Item.Getitem() is Infinitebomb)) {
					Setbombs(Bombs + Item.Getitem().Getcount());
					Item.Getitem().Onpickup();
					Item.Remove();
					Item.Done = true;
					this.Playsfx("pickup_item");

					for (int J = 0; J < 3; J++) {
						Pooffx Fx = new Pooffx();
						Fx.X = Item.X + Item.W / 2;
						Fx.Y = Item.Y + Item.H / 2;
						Dungeon.Area.Add(Fx);
					}

					return true;
				} else if (Item.Getitem() is Gold) {
					Setmoney(Money + Item.Getitem().Getcount());
					Item.Getitem().Onpickup();
					Item.Remove();
					Item.Done = true;
					this.Playsfx("pickup_item");

					for (int J = 0; J < 3; J++) {
						Pooffx Fx = new Pooffx();
						Fx.X = Item.X + Item.W / 2;
						Fx.Y = Item.Y + Item.H / 2;
						Dungeon.Area.Add(Fx);
					}

					return true;
				} else if (Item.Getitem() is Key && !(Item.Getitem() is Burningkey)) {
					Setkeys(Keys + Item.Getitem().Getcount());
					Item.Getitem().Onpickup();
					Item.Remove();
					Item.Done = true;
					this.Playsfx("pickup_item");

					for (int J = 0; J < 3; J++) {
						Pooffx Fx = new Pooffx();
						Fx.X = Item.X + Item.W / 2;
						Fx.Y = Item.Y + Item.H / 2;
						Dungeon.Area.Add(Fx);
					}

					return true;
				} else if (Item.Getitem() is Weaponbase) {
					if (Inventory.Isempty(0)) {
						Inventory.Setslot(0, Item.Getitem());
						Item.Getitem().Setowner(this);
						Item.Getitem().Onpickup();
						Item.Remove();
						Item.Done = true;
						this.Playsfx("pickup_item");

						for (int J = 0; J < 3; J++) {
							Pooffx Fx = new Pooffx();
							Fx.X = Item.X + Item.W / 2;
							Fx.Y = Item.Y + Item.H / 2;
							Dungeon.Area.Add(Fx);
						}

						return true;
					} else if (Inventory.Isempty(1)) {
						Inventory.Setslot(1, Item.Getitem());
						Item.Getitem().Setowner(this);
						Item.Getitem().Onpickup();
						Item.Remove();
						Item.Done = true;
						this.Playsfx("pickup_item");

						for (int J = 0; J < 3; J++) {
							Pooffx Fx = new Pooffx();
							Fx.X = Item.X + Item.W / 2;
							Fx.Y = Item.Y + Item.H / 2;
							Dungeon.Area.Add(Fx);
						}

						return true;
					} else {
						Item It = Item.Getitem();
						Item.Setitem(this.Inventory.Getslot(this.Inventory.Active));
						this.Inventory.Setslot(this.Inventory.Active, It);
						It.Setowner(this);
						It.Onpickup();
						this.Playsfx("pickup_item");

						return false;
					}

				} else if (Item.Getitem() is Activeitem || (Item.Getitem() is Consumable && !(Item.Getitem() is Autouse))) {
					if (Inventory.Getslot(2) == null) {
						Inventory.Setslot(2, Item.Getitem());
						Item.Getitem().Setowner(this);
						Item.Getitem().Onpickup();
						Item.Remove();
						Item.Done = true;
						this.Playsfx("pickup_item");

						for (int J = 0; J < 3; J++) {
							Pooffx Fx = new Pooffx();
							Fx.X = Item.X + Item.W / 2;
							Fx.Y = Item.Y + Item.H / 2;
							Dungeon.Area.Add(Fx);
						}

						return true;
					} else {
						Item It = Item.Getitem();
						Item.Setitem(this.Inventory.Getslot(2));
						this.Inventory.Setslot(2, It);
						It.Setowner(this);
						It.Onpickup();
						this.Playsfx("pickup_item");

						return false;
					}

				} else {
					Item It = Item.Getitem();
					It.Setowner(this);
					It.Onpickup();
					this.Playsfx("pickup_item");
					Inventory.Add(Item);

					return true;
				}

			} 

			return false;
		}

		public bool Didgethit() {
			return Gothit;
		}

		public int Getmanamax() {
			return this.Manamax;
		}

		public Inventory Getinventory() {
			return this.Inventory;
		}

		public override Void Update(float Dt) {
			base.Update(Dt);
			Light.Setactive(true);
			Light.Attachtobody(Body, 8, 8, 0);
			Light.Setposition(X + 8, Y + 8);
			Light.Setdistance(180);

			if (this.Hasbuff(Burningbuff.Gettype())) {
				this.Light.Setcolor(1, 0.5f, 0f, 1);
			} else {
				this.Light.Setcolor(1, 1, 0.8f, 1);
			}


			if (!this.Rolling) {
				if (this.Isflying() || this.Touches[Terrain.Wall] || this.Touches[Terrain.Floor_a] || this.Touches[Terrain.Floor_b] || this.Touches[Terrain.Floor_c] || this.Touches[Terrain.Floor_d] || this.Touches[Terrain.Disco]) {
					this.Onground = true;
					this.Lastground.X = this.X;
					this.Lastground.Y = this.Y;
				} 

				if (!this.Onground) {
					this.Teleport = true;

					for (int I = 0; I < 5; I++) {
						Pooffx Fx = new Pooffx();
						Fx.X = this.X + this.W / 2;
						Fx.Y = this.Y + this.H / 2;
						Dungeon.Area.Add(Fx);
					}

					this.Dotp(false);

					for (int I = 0; I < 5; I++) {
						Pooffx Fx = new Pooffx();
						Fx.X = this.X + this.W / 2;
						Fx.Y = this.Y + this.H / 2;
						Dungeon.Area.Add(Fx);
					}

					this.Teleport = false;
					this.Modifyhp(-1, null, true);
				} 

				this.Onground = false;
			} 

			this.Z = Math.Max(0, this.Zvel * Dt + this.Z);
			this.Zvel = this.Zvel - Dt * 220;
			Orbitalring.Lerp(new Vector2(this.X + this.W / 2, this.Y + this.H / 2), 4 * Dt);

			if (this.Todeath) {
				this.T += Dt;
				this.Animation.Update(Dt * (this.Flipped != this.Acceleration.X < 0 && this.Animation == Run ? -1 : 1));

				if (this.T >= 1f) {
					Ui.Ui.Dead = true;
					base.Die(false);
					this.Dead = true;
					this.Done = true;
					Camera.Shake(10);
					this.Remove();
					Deatheffect(Killed);
					Bloodfx.Add(this, 20);
					List<Item> Items = new List<>();

					for (int I = 0; I < 3; I++) {
						if (Inventory.Getslot(I) != null) {
							Items.Add(Inventory.Getslot(I));
						} 
					}

					for (int I = 0; I < Inventory.Getspace(); I++) {
						Items.Add(Inventory.Getspace(I));
					}

					foreach (Item Item in Items) {
						Itemholder Holder = new Itemholder();
						Holder.X = this.X + Random.Newfloat(16);
						Holder.Y = this.Y + Random.Newfloat(16);
						Holder.Randomvelocity();
						Holder.Setitem(Item);
						Dungeon.Area.Add(Holder);
					}

					Savemanager.Delete();
					Inventory.Clear();
				} 

				return;
			} 

			if (this.Mana != this.Manamax) {
				bool Dark = Player.Instance.Isdead();

				if (!Dark) {
					Dark = Boss.All.Size() > 0 && Player.Instance.Room is Bossroom && !Burningknight.Instance.Rage;

					if (!Dark) {
						foreach (Mob Mob in Mob.All) {
							if (Mob.Room == Player.Instance.Room) {
								Dark = true;

								break;
							} 
						}
					} 
				} 
			} 

			if (this.Dead) {
				base.Common();

				return;
			} 

			if (this.Hp <= 2) {
				this.Last += Dt;
				this.Lastblood += Dt;

				if (this.Lastblood > 0.1f) {
					this.Lastblood = 0;
					Blooddropfx Fx = new Blooddropfx();
					Fx.Owner = this;
					Dungeon.Area.Add(Fx);
				} 

				if (this.Last >= 1f && Settings.Blood) {
					this.Last = 0;
					Bloodsplatfx Fxx = new Bloodsplatfx();
					Fxx.X = X + Random.Newfloat(W) - 8;
					Fxx.Y = Y + Random.Newfloat(H) - 8;
					Dungeon.Area.Add(Fxx);
				} 
			} 

			Item Item = this.Inventory.Getslot(this.Inventory.Active);

			if (Item != null) {
				Item.Updateinhands(Dt);
			} 

			this.Heat = Math.Max(0, this.Heat - Dt / 3);

			if (!Sucked && Dialog.Active == null && !this.Freezed && !Uimap.Large) {
				if (!this.Rolling) {
					if (Input.Instance.Isdown("left")) {
						this.Acceleration.X -= this.Speed;
					} 

					if (Input.Instance.Isdown("right")) {
						this.Acceleration.X += this.Speed;
					} 

					if (Input.Instance.Isdown("up")) {
						this.Acceleration.Y += this.Speed;
					} 

					if (Input.Instance.Isdown("down")) {
						this.Acceleration.Y -= this.Speed;
					} 

					Vector2 Move = Input.Instance.Getaxis("move");

					if (Move.Len2() > 0.2f) {
						this.Acceleration.X += Move.X * this.Speed;
						this.Acceleration.Y -= Move.Y * this.Speed;
					} 
				} 

				if (!this.Rolling) {
					if (Input.Instance.Waspressed("roll")) {
						Rolled = true;
						float F = 80;
						Ignoreacceleration = true;

						if (Acceleration.Len() > 1f) {
							double A = (Math.Atan2(Acceleration.Y, Acceleration.X));
							Acceleration.X = (float) Math.Cos(A) * Speed * F;
							Acceleration.Y = (float) Math.Sin(A) * Speed * F;
						} else {
							double A = (Getangleto(Input.Instance.Worldmouse.X, Input.Instance.Worldmouse.Y));
							Acceleration.X = (float) Math.Cos(A) * Speed * F;
							Acceleration.Y = (float) Math.Sin(A) * Speed * F;
						}


						for (int I = 0; I < 3; I++) {
							Pooffx Fx = new Pooffx();
							Fx.X = this.X + this.W / 2;
							Fx.Y = this.Y + this.H / 2;
							Fx.T = 0.5f;
							Dungeon.Area.Add(Fx);
						}

						Playsfx("dash_short");
						Player Self = this;
						Tween.To(new Tween.Task(0, 0.2f) {
							public override Void Onend() {
								Removebuff(Burningbuff.Gettype());
								Ignoreacceleration = false;
								Self.Velocity.X = 0;
								Self.Velocity.Y = 0;
								Tween.To(new Tween.Task(0, 0) {
									public override Void Onstart() {
										Rolling = false;
									}

									public override Void Onend() {
										base.Onend();
										Animation = Idle;
									}
								}).Delay(0.05f);
							}
						}).Delay(0.05f);
						this.Rolling = true;
						this.Velocity.X = 0;
						this.Velocity.Y = 0;
					} 
				} 
			} else if (Dialog.Active != null) {
				if (Input.Instance.Waspressed("interact")) {
					Dialog.Active.Skip();
				} 
			} 

			float V = this.Acceleration.Len2();

			if (Knockback.Len() + V > 4f) {
				this.Stopt = 0;
			} else {
				Stopt += Dt;
			}


			if (V > 20) {
				this.Become("run");
			} else {
				this.Become("idle");
			}


			base.Common();

			if (this.Animation != null && !this.Freezed) {
				if (this.Animation.Update(Dt)) {

				} 
			} 

			if (this.Isrolling()) {
				Lastfx += Dt;

				if (Lastfx >= 0.05f) {
					Pooffx Fx = new Pooffx();
					Fx.X = this.X + this.W / 2;
					Fx.Y = this.Y + this.H / 2;
					Fx.T = 0.5f;
					Dungeon.Area.Add(Fx);
					Lastfx = 0;
				} 
			} 

			if (!this.Freezed) {
				float Dx = this.X + this.W / 2 - Input.Instance.Worldmouse.X;
				this.Flipped = Dx >= 0;
			} 

			int I = Level.Toindex(Math.Round((this.X) / 16), Math.Round((this.Y + this.H / 2) / 16));

			if (this.Burnlevel > 0) {
				Dungeon.Level.Setonfire(I, true);
			} 

			if (this.Frostlevel > 0) {
				Dungeon.Level.Freeze(I);

				if (this.Frostlevel >= 4) {
					if (Dungeon.Level.Liquiddata[I] == Terrain.Lava) {
						Dungeon.Level.Set(I, Terrain.Ice);
						Dungeon.Level.Updatetile(Level.Tox(I), Level.Toy(I));
					} 
				} 
			} 
		}

		public int Getmana() {
			return (int) this.Mana;
		}

		public int Getlevel() {
			return this.Level;
		}

		public override bool Isflying() {
			return Flight > 0 || this.Rolling;
		}

		public Void Checksecrets() {
			if (this.Seesecrets) {
				if (Room != null) {
					foreach (Room R in Room.Connected.Keyset()) {
						if (R.Hidden) {
							for (int Y = R.Top; Y <= R.Bottom; Y++) {
								for (int X = R.Left; X <= R.Right; X++) {
									if (Dungeon.Level.Get(X, Y) == Terrain.Crack) {
										R.Hidden = false;
										Bombentity.Make(R);
										Dungeon.Level.Set(X, Y, Terrain.Floor_a);
										Dungeon.Level.Loadpassable();
										Dungeon.Level.Addphysics();
									} 
								}
							}
						} 
					}
				} 
			} 
		}

		public override Hpfx Modifyhp(int Amount, Creature From) {
			if (Amount > 0 && this.Hp + Amount > 1) {
				Tween.To(new Tween.Task(0, 0.4f) {
					public override float Getvalue() {
						return Dungeon.Blood;
					}

					public override Void Setvalue(float Value) {
						Dungeon.Blood = Value;
					}
				});
			} 

			return base.Modifyhp(Amount, From);
		}

		public override float Rolldamage() {
			return Getdamagemodifier();
		}

		public float Getdamagemodifier() {
			return 1;
		}

		public override Void Onhurt(int A, Entity From) {
			base.Onhurt(A, From);
			this.Gothit = true;
			Dungeon.Flash(Color.White, 0.05f);
			Camera.Shake(4f);
			Audio.Playsfx("voice_gobbo_" + Random.Newint(1, 4), 1f, Random.Newfloat(0.9f, 1.9f));
		}

		public override Void Onbuffremove(Buff Buff) {
			base.Onbuffremove(Buff);

			foreach (Uibuff B in this.Uibuffs) {
				if (B.Buff.Getclass().Equals(Buff.Getclass())) {
					B.Remove();

					return;
				} 
			}
		}

		public override Void Save(Filewriter Writer) {
			base.Save(Writer);
			this.Inventory.Save(Writer);
			Writer.Writeint32((int) this.Mana);
			Writer.Writeint32(this.Manamax);
			Writer.Writeint32(this.Level);
			Writer.Writefloat(this.Speed);
			Writer.Writebyte((byte) Numironhearts);
			Writer.Writebyte((byte) Numgoldenhearts);
			Writer.Writeboolean(this.Gothit);
			Writer.Writebyte((byte) this.Bombs);
			Writer.Writebyte((byte) this.Keys);
			Writer.Writeint16((short) this.Money);
		}

		public override Void Load(Filereader Reader) {
			base.Load(Reader);
			this.Inventory.Load(Reader);
			Reader.Readint32();
			this.Manamax = Reader.Readint32();
			this.Mana = Manamax;
			this.Level = Reader.Readint32();
			float Last = this.Speed;
			this.Speed = Reader.Readfloat();
			this.Numironhearts = Reader.Readbyte();
			this.Numgoldenhearts = Reader.Readbyte();
			this.Gothit = Reader.Readboolean();
			this.Maxspeed += (this.Speed - Last) * 7f;
			this.Sethat(null);
			Dotp(false);
			Hasbkkey = this.Inventory.Find(Burningkey.Gettype());
			Onroomchange();
			this.Bombs = Reader.Readbyte();
			this.Keys = Reader.Readbyte();
			this.Money = Reader.Readint16();
			Light.Setposition(this.X + 8, this.Y + 8);
			Light.Attachtobody(this.Body, 8, 8, 0);
		}

		public override Void Addbuff(Buff Buff) {
			if (this.Canhavebuff(Buff)) {
				Buff B = this.Buffs.Get(Buff.Getclass());

				if (B != null) {
					B.Setduration(Math.Max(B.Getduration(), Buff.Getduration()));
				} else {
					this.Buffs.Put(Buff.Getclass(), Buff);
					Buff.Setowner(this);
					Buff.Onstart();
					Uibuff Bf = new Uibuff();
					Bf.Buff = Buff;
					Bf.Owner = this;

					foreach (Uibuff Bu in this.Uibuffs) {
						if (Bu.Buff.Getclass() == Buff.Getclass()) {
							Bu.Buff = Buff;

							return;
						} 
					}

					this.Uibuffs.Add(Bf);
				}

			} 
		}

		protected override Void Common() {
			base.Common();
		}

		protected override Void Ontouch(short T, int X, int Y, int Info) {
			if (T == Terrain.Water && !this.Isflying()) {
				if (this.Hasbuff(Burningbuff.Gettype())) {
					int Num = Globalsave.Getint("num_fire_out") + 1;
					Globalsave.Put("num_fire_out", Num);

					if (Num >= 50) {
						Achievements.Unlock(Achievements.Unlock_water_bolt);
					} 

					this.Removebuff(Burningbuff.Gettype());

					for (int I = 0; I < 20; I++) {
						Steamfx Fx = new Steamfx();
						Fx.X = this.X + Random.Newfloat(this.W);
						Fx.Y = this.Y + Random.Newfloat(this.H);
						Dungeon.Area.Add(Fx);
					}
				} 

				if (this.Leavevenom > 0) {
					Dungeon.Level.Venom(X, Y);
				} 
			} else {
				if (!this.Isflying() && Bithelper.Isbitset(Info, 0) && !this.Hasbuff(Burningbuff.Gettype())) {
					this.Addbuff(new Burningbuff());
				} 

				if (T == Terrain.Lava && !this.Isflying() && this.Lavaresist == 0) {
					this.Modifyhp(-1, null, true);
					this.Addbuff(new Burningbuff());

					if (this.Isdead()) {
						Achievements.Unlock(Achievements.Unlock_wings);
					} 
				} else if (!this.Isflying() && (T == Terrain.High_grass || T == Terrain.High_dry_grass)) {
					Dungeon.Level.Set(X, Y, T == Terrain.High_grass ? Terrain.Grass : Terrain.Dry_grass);

					for (int I = 0; I < 10; I++) {
						Grassbreakfx Fx = new Grassbreakfx();
						Fx.X = X * 16 + Random.Newfloat(16);
						Fx.Y = Y * 16 + Random.Newfloat(16) - 8;
						Dungeon.Area.Add(Fx);
					}
				} else if (!this.Isflying() && T == Terrain.Venom) {
					this.Addbuff(new Poisonbuff());
				} 
			}

		}

		protected override Void Onroomchange() {
			base.Onroomchange();
			Bot.Data.Clear();
			Ingamestate.Checkmusic();

			if (Dungeon.Depth > -1) {
				if (Numcollectedhearts >= 6) {
					Achievements.Unlock(Achievements.Unlock_meatboy);
				} 

				if (Hadenemies && !Gothit) {
					Achievements.Unlock(Achievements.Unlock_halo);
				} 
			} 

			this.Resethit();

			if (this.Room != null) {
				if (this.Room is Shoproom) {
					Audio.Play("Shopkeeper");

					if (Burningknight.Instance != null && !Burningknight.Instance.Getstate().Equals("unactive")) {
						Burningknight.Instance.Become("await");
					} 
				} 

				Hadenemies = false;
			} 

			this.Checksecrets();

			if (Room != null) {
				int Count = 0;

				foreach (Mob Mob in Mob.All) {
					if (Mob.Room == Room) {
						Count++;
					} 
				}

				if (Count > 0) {
					this.Invt = Math.Max(this.Invt, 1f);
				} 
			} 
		}

		protected override bool Ignorewater() {
			return Slowliquidresist > 0;
		}

		protected override Void Checkdeath() {
			if (this.Hp == 0 && this.Numironhearts == 0 && this.Numgoldenhearts == 0) {
				this.Shoulddie = true;
			} 
		}

		protected override Void Dohurt(int A) {
			if (this.Numgoldenhearts > 0) {
				int D = Math.Min(this.Numgoldenhearts, -A);
				this.Numgoldenhearts -= D;
				A += D;

				for (int I = 0; I < 10; I++) {
					Pooffx Fx = new Pooffx();
					Fx.X = this.X + this.W / 2;
					Fx.Y = this.Y + this.H / 2;
					Dungeon.Area.Add(Fx);
				}

				for (int I = 0; I < 10; I++) {
					Pooffx Fx = new Pooffx();
					Fx.X = this.X + this.W / 2;
					Fx.Y = this.Y + this.H / 2;
					Dungeon.Area.Add(Fx);
				}

				foreach (Mob Mob in Mob.All) {
					if (Mob.Room == this.Room) {
						Mob.Addbuff(new Freezebuff().Setduration(10));
					} 
				}
			} 

			if (this.Numironhearts > 0) {
				int D = Math.Min(this.Numironhearts, -A);
				this.Numironhearts -= D;
				A += D;
			} 

			if (A < 0) {
				this.Hp = Math.Max(0, this.Hp + A);
			} 
		}

		protected override Void Die(bool Force) {
			if (this.Todeath) {
				return;
			} 

			Uimap.Instance.Hide();
			Ui.Ui.Ondeath();
			this.Done = false;
			int Num = Globalsave.Getint("deaths") + 1;
			Globalsave.Put("deaths", Num);
			Vector3 Vec = Camera.Game.Project(new Vector3(this.X + this.W / 2, this.Y + this.H / 2, 0));
			Vec = Camera.Ui.Unproject(Vec);
			Vec.Y = Display.Ui_height - Vec.Y;
			Dungeon.Shocktime = 0;
			Dungeon.Shockpos.X = (Vec.X) / Display.Ui_width;
			Dungeon.Shockpos.Y = (Vec.Y) / Display.Ui_height;
			this.Todeath = true;
			this.T = 0;
			Dungeon.Slowdown(0.5f, 1f);

			if (Dungeon.Depth != -3) {
				Achievements.Unlock(Achievements.Die);
			} 

			if (Num >= 50) {
				Achievements.Unlock(Achievements.Unlock_isaac_head);
			} 
		}

		protected override bool Canhavebuff(Buff Buff) {
			if ((this.Rolling || Fireresist > 0) && Buff is Burningbuff) {
				return false;
			} else if (Poisonresist > 0 && Buff is Poisonbuff) {
				return false;
			} else if ((this.Rolling || Stunresist > 0) && Buff is Freezebuff) {
				return false;
			} 

			return base.Canhavebuff(Buff);
		}

		private Void Dotp(bool Frominit) {
			if (this.Teleport) {
				this.Tp(this.Lastground.X, this.Lastground.Y);

				return;
			} 

			if (Dungeon.Depth == -3) {

			} else if (Dungeon.Depth == -1) {
				Room Room = Dungeon.Level.Getrooms().Get(0);
				this.Tp((Room.Left + Room.Getwidth() / 2) * 16 - 8, Room.Top * 16 + 32);
			} else if (Ladder != null && (Dungeon.Loadtype != Entrance.Loadtype.Loading || (!Frominit && (Dungeon.Level.Findroomfor(this.X + this.W / 2, this.Y) == null)))) {
				this.Tp(Ladder.X, Ladder.Y - 4);
			} else if (Ladder == null) {
				Log.Error("Null lader!");
			} 

			Vector3 Vec = Camera.Game.Project(new Vector3(Player.Instance.X + Player.Instance.W / 2, Player.Instance.Y + Player.Instance.H / 2, 0));
			Vec = Camera.Ui.Unproject(Vec);
			Vec.Y = Display.Game_height - Vec.Y / Display.Ui_scale;
			Dungeon.Darkx = Vec.X / Display.Ui_scale;
			Dungeon.Darky = Vec.Y;
		}
	}
}
