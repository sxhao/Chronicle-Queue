/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle;

import net.openhft.chronicle.tools.CheckedExcerpt;
import net.openhft.chronicle.tools.ChronicleTools;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class VanillaChronicle3Test extends VanillaChronicleTestBase {

    @Test
    public void testFinishAfterClose() throws IOException {
        final String basePath = getTestPath();
        final VanillaChronicle chronicle = (VanillaChronicle)ChronicleQueueBuilder.vanilla(basePath).build();
        final ExcerptAppender appender = chronicle.createAppender();
        final ExcerptTailer tailer = chronicle.createTailer();

        try {
            appender.startExcerpt(8);
            appender.writeLong(1);
            appender.close();

            assertTrue(appender.isFinished());

            for(long i=0;i<5;i++) {
                appender.startExcerpt(8);
                appender.writeLong(i);
                appender.close();
            }

            appender.startExcerpt(8);
            appender.writeLong(999);
            appender.finish();
            appender.close();

            assertTrue(tailer.nextIndex());
            assertEquals(999, tailer.readLong());
            tailer.finish();

            assertFalse(tailer.nextIndex());
            tailer.close();
        } finally {
            chronicle.checkCounts(1, 1);
            chronicle.close();
            chronicle.clear();

            assertFalse(new File(basePath).exists());
        }
    }

    /*
     * https://higherfrequencytrading.atlassian.net/browse/CHRON-92
     */
    @Test
    public void testJira92() throws Exception {
        final int RUNS = 10;
        final int indicesPerFile = 2;

        final String baseDir = getTestPath();
        final File baseFile = new File(baseDir, new SimpleDateFormat("yyyyMMdd").format(new Date()));
        final Set<Long> indices = new HashSet<>(RUNS);

        final Chronicle chronicle = ChronicleQueueBuilder.vanilla(baseDir)
            .defaultMessageSize(128)
            .indexBlockSize(indicesPerFile * 8)
            .dataBlockSize(16 * 1024)
            .build();

        chronicle.clear();

        try {
            final ExcerptAppender appender = chronicle.createAppender();
            final ExcerptTailer tailer = chronicle.createTailer();
            final Excerpt excerpt = chronicle.createExcerpt();

            for (long entry = 0; entry < RUNS; entry++) {
                appender.startExcerpt();
                appender.writeLong(entry);
                appender.finish();

                long idx = appender.lastWrittenIndex();
                int idxFileNum = findLastIndexCacheNumber(baseFile);

                assertTrue("Index should be unique (" + idx + ")", indices.add(idx));
                assertEquals(entry / indicesPerFile, idxFileNum);

                tailer.nextIndex();
                assertEquals(entry, tailer.readLong());
                assertEquals(tailer.index(), idx);
                tailer.finish();

                excerpt.index(idx);
                assertEquals(entry, excerpt.readLong());
                assertEquals(excerpt.index(), idx);
                excerpt.finish();
            }

            appender.close();
            tailer.close();
            excerpt.close();

            ChronicleTools.checkCount(chronicle, 1, 1);
        } finally {
            chronicle.close();
            chronicle.clear();

            assertFalse(new File(baseDir).exists());
        }
    }

    @Test
    public void testCheckedVanillaExcerpt_001() throws IOException {
        final String basePath = getTestPath();
        final VanillaChronicle chronicle = (VanillaChronicle)ChronicleQueueBuilder.vanilla(basePath)
            .useCheckedExcerpt(true)
            .build();

        final ExcerptAppender appender = chronicle.createAppender();

        assertTrue(appender instanceof CheckedExcerpt);

        try {
            appender.startExcerpt(8);
            appender.writeLong(1);

            testByte(appender);
            testChar(appender);
            testShort(appender);
            testInt(appender);
            testLong(appender);
            testDouble(appender);
            testObject(appender);

            appender.finish();
        } finally {
            appender.close();

            chronicle.checkCounts(1, 1);
            chronicle.close();
            chronicle.clear();

            assertFalse(new File(basePath).exists());
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    private void testByte(final ExcerptAppender appender) {
        try {
            appender.writeByte(1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }

        try {
            appender.writeByte(8,1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

    private void testChar(final ExcerptAppender appender) {
        try {
            appender.writeChar(1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }

        try {
            appender.writeChar(8,1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

    private void testShort(final ExcerptAppender appender) {
        try {
            appender.writeShort(1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }

        try {
            appender.writeShort(8,1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

    private void testInt(final ExcerptAppender appender) {
        try {
            appender.writeInt(1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }

        try {
            appender.writeOrderedInt(1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }

        try {
            appender.writeInt(8,1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }

        try {
            appender.writeOrderedInt(8,1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }

        try {
            appender.compareAndSwapInt(8,1,2);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

    private void testLong(final ExcerptAppender appender) {
        try {
            appender.writeLong(1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }

        try {
            appender.writeOrderedLong(1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }

        try {
            appender.writeLong(8,1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }

        try {
            appender.writeOrderedLong(8,1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }

        try {
            appender.compareAndSwapLong(8,1,2);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

    private void testDouble(final ExcerptAppender appender) {
        try {
            appender.writeDouble(1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }

        try {
            appender.writeDouble(8,1);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

    private void testObject(final ExcerptAppender appender) {
        try {
            appender.writeObject("1234567890",0,5);
            fail("expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }
}
