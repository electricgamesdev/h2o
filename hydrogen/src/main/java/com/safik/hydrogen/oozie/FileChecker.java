package com.safik.hydrogen.oozie;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class FileChecker {

	public static void main(String[] args) {
		final String source_pattern = "name.substring(file.lastIndexOf('_'),file.lastIndexOf('.')";// "(name.split('_')[6]).substring(0,12)";
		final String primary_key = "201312031234";

		DistributedFileSystem fileSystem;
		try {
			fileSystem = (DistributedFileSystem) DistributedFileSystem
					.get(new Configuration());
			
			PathFilter filter = new PathFilter() {

				public boolean accept(Path path) {

					ExpressionParser parser = new SpelExpressionParser();
					StandardEvaluationContext fileContext = new StandardEvaluationContext(
							path);

					Expression exp = parser.parseExpression(source_pattern);
					String message = exp.getValue(fileContext, String.class);
					System.out.println(message);

					boolean source_key_valid = primary_key
							.equalsIgnoreCase(message);

					return source_key_valid;
				}

			};
			FileStatus[] fs = fileSystem.listStatus(new Path(
					"/user/hydrogen/source"), filter);

			System.out.println("size = " + fs.length);
			// iterate entity check
			Path to = new Path("/user/hydrogen/target/" + primary_key);
			if (!fileSystem.exists(to)) {
				fileSystem.mkdirs(to);
				for (int i = 0; i < fs.length; i++) {
					String entity_file = fs[i].getPath().getName();
					fileSystem.rename(fs[i].getPath(),new Path(to.toString()+Path.SEPARATOR+entity_file));
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
