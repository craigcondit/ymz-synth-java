package org.randomcoder.ymzsynth.util;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;

public class YMZInfo
{
	private static final long NS_PER_CLOCK = 125L;

	public static void main(String[] args) throws Exception
	{
		if (args.length < 1)
		{
			System.err.println("Usage: " + YMZInfo.class.getName() + " <directory>");
		}
		String dir = args[0];

		Files.walkFileTree(new File(dir).toPath(), new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				if (attrs.isRegularFile() && file.getFileName().toString().toLowerCase(Locale.US).endsWith(".ymz"))
				{
					long registerCount = 0L;
					long clockCount = 0L;
					long prevClock = 1L;
					long badRegisterCount = 0L;
					try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file.toFile()), 1048576)))
					{
						while (true)
						{
							long clock = dis.readLong();
							byte register = dis.readByte();
							byte value = dis.readByte();
							registerCount++;
							if (clock != prevClock)
							{
								prevClock = clock;
								clockCount++;
							}
							if ((register & 0xff) > 0x0d)
							{
								badRegisterCount++;
							}
						}
					}
					catch (EOFException ignored)
					{}
					long timeInSeconds = prevClock * NS_PER_CLOCK / 1_000_000_000L;
					long registersSec = timeInSeconds > 0 ? (registerCount / timeInSeconds) : 0;
					long clockSec = timeInSeconds > 0 ? (clockCount / timeInSeconds) : 0;

					String msg = file.toString() + ": registers=" + registerCount + " clocks=" + clockCount + " bad_registers=" + badRegisterCount + " time_in_sec="
							+ timeInSeconds + " registers/sec=" + registersSec + " clocks/sec=" + clockSec;
					if (badRegisterCount > 0L)
					{
						System.err.println(msg);
					}
					else
					{
						System.out.println(msg);
					}
				}
				return FileVisitResult.CONTINUE;
			}

		});
	}
}
