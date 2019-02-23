using org.rexcellentgames.byejava.ast;
using java.util;

namespace org.rexcellentgames.byejava.emitter {
	public class Emitter {
		private ArrayList<Statement> ast;
		private StringBuilder builder;

		public Emitter(ArrayList ast) {
			this.setAst(ast);
		}

		public void setAst(ArrayList ast) {
			this.ast = ast;
		}

		public String emit() {
			this.builder = new StringBuilder();
			int tabs = 0;
			Statement packageStatement = null;
			ArrayList<Statement.Import> imports = new ArrayList<>();

			foreach (Statement statement in this.ast) {
				if statement is Statement.Package {
					packageStatement = statement;
				} else if statement is Statement.Import {
					Statement.Import im = (Statement.Import) statement;
					boolean found = false;

					foreach (Statement.Import i in imports) {
						if i.module.equals(im.module) {
							found = true;

							break;
						} 					}

					if !found {
						imports.add(im);
						im.emit(builder, tabs);
					} 				} 			}

			if imports.size() > 0 {
				builder.append('\n');
			} 
			if packageStatement != null {
				tabs = packageStatement.emit(builder, tabs);
			} 
			foreach (Statement statement in this.ast) {
				if statement != packageStatement && !(statement is Statement.Import) {
					tabs = statement.emit(this.builder, tabs);
				} 			}

			if packageStatement != null {
				tabs = packageStatement.emitEnd(this.builder, tabs);
			} 
			return this.builder.toString();
		}
	}
}
