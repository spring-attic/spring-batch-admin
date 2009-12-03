/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.admin.sample;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineCallbackHandler;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

public class LeadRandomizer {

	@Test
	public void testLeads() throws Exception {

		FlatFileItemReader<FieldSet> reader = new FlatFileItemReader<FieldSet>();
		reader.setResource(new ClassPathResource("/data/test.txt"));
		DefaultLineMapper<FieldSet> lineMapper = new DefaultLineMapper<FieldSet>();
		lineMapper.setLineTokenizer(new DelimitedLineTokenizer());
		lineMapper.setFieldSetMapper(new PassThroughFieldSetMapper());
		reader.setLinesToSkip(1);
		final List<String> headers = new ArrayList<String>();
		reader.setSkippedLinesCallback(new LineCallbackHandler() {
			public void handleLine(String line) {
				headers.add(line);
			}
		});
		reader.setLineMapper(lineMapper);
		reader.open(new ExecutionContext());

		List<FieldSet> list = new ArrayList<FieldSet>();
		FieldSet item = reader.read();
		while (item!=null) {
			list.add(item);
			item = reader.read();
		}
		assertEquals(7, list.size());

		FlatFileItemWriter<FieldSet> writer = new FlatFileItemWriter<FieldSet>();
		FileSystemResource resource = new FileSystemResource("target/output/output.txt");
		FileUtils.deleteQuietly(resource.getFile());
		writer.setResource(resource);
		writer.setHeaderCallback(new FlatFileHeaderCallback() {
			public void writeHeader(Writer writer) throws IOException {
				for (String header : headers) {
					writer.write(header);
				}
			}
		});
		writer.setLineAggregator(new DelimitedLineAggregator<FieldSet>());
		writer.open(new ExecutionContext());
		
		String[] names = getFields(list, 1);
		String[] country = getFields(list, 2);
		String[] products = getFields(list, 3);
		double[] amounts = getMinMax(list, 4);
				
		NumberFormat formatter = new DecimalFormat("#.##");
		int count = 20;
		for (int i=0; i<100; i++) {
			List<FieldSet> items = new ArrayList<FieldSet>();
			for( FieldSet fieldSet : list) {
				String[] values = fieldSet.getValues();
				values[0] = ""+(count++);
				values[1] = choose(names);
				values[2] = choose(country);
				values[3] = choose(products);
				values[4] = formatter.format(random(amounts));
				items.add(new DefaultFieldSet(values));
			}
			writer.write(items);
		}
		writer.close();
		
	}

	private String choose(String[] names) {
		return names[(int)(Math.random()*(names.length))];
	}

	private BigDecimal random(double[] amounts) {
		return new BigDecimal((int)((Math.random()*(amounts[1]-amounts[0]) + amounts[0])*100)).divide(new BigDecimal(100));
	}

	private String[] getFields(List<FieldSet> list, int column) {
		List<String> values = new ArrayList<String>();
		for(FieldSet fieldSet : list) {
			values.add(fieldSet.readString(column));
		}
		return values.toArray(new String[values.size()]);
	}

	private double[] getMinMax(List<FieldSet> list, int column) {
		double min = 0;
		double max = 0;
		for(FieldSet fieldSet : list) {
			double x = fieldSet.readDouble(column);
			if (x>max) max = x;
			if (x<min) min = x;
		}
		return new double[] {min, max};
	}

}
