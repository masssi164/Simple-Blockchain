package de.flashyotter.blockchain_node.storage;

import blockchain.core.model.Block;
import blockchain.core.model.Transaction;
import blockchain.core.serialization.JsonUtils;
import de.flashyotter.blockchain_node.config.NodeProperties;
import jakarta.annotation.PreDestroy;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

/** Persistent LevelDB-based BlockStore. */
@Component
public class LevelDbBlockStore implements BlockStore, AutoCloseable {
    private final DB db;

    public LevelDbBlockStore(NodeProperties props) {
        try {
            Path dir = Path.of(props.getDataPath(), "blocks");
            Files.createDirectories(dir);
            Options options = new Options();
            options.createIfMissing(true);
            this.db = factory.open(dir.toFile(), options);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open LevelDB", e);
        }
    }

    @Override
    public void save(Block b) {
        db.put(b.getHashHex().getBytes(StandardCharsets.UTF_8), encode(b));
    }

    @Override
    public Block findByHash(String hash) {
        byte[] data = db.get(hash.getBytes(StandardCharsets.UTF_8));
        if (data == null) return null;
        return decode(data);
    }

    @Override
    public Iterable<Block> loadAll() {
        List<Block> blocks = new ArrayList<>();
        try (DBIterator it = db.iterator()) {
            for (it.seekToFirst(); it.hasNext(); it.next()) {
                Map.Entry<byte[], byte[]> entry = it.peekNext();
                blocks.add(decode(entry.getValue()));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to iterate DB", e);
        }
        return blocks;
    }

    private byte[] encode(Block b) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(b);
            oos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize Block", e);
        }
    }

    private Block decode(byte[] bytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (Block) ois.readObject();
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize Block", e);
        }
    }

    @PreDestroy
    @Override
    public void close() throws IOException {
        db.close();
    }
}
