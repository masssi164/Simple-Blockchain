package de.flashyotter.blockchain_node.storage;

import blockchain.core.model.Block;
import de.flashyotter.blockchain_node.config.NodeProperties;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

/** BlockStore that writes all blocks to an append-only log before delegating to LevelDB. */
@Component
@Primary
public class WriteAheadLogBlockStore implements BlockStore, AutoCloseable {
    private final LevelDbBlockStore db;
    private final Path logPath;
    private final FileChannel logChannel;

    public WriteAheadLogBlockStore(LevelDbBlockStore db, NodeProperties props) {
        this.db = db;
        try {
            this.logPath = Path.of(props.getDataPath(), "block.log");
            Files.createDirectories(logPath.getParent());
            this.logChannel = FileChannel.open(logPath,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open block log", e);
        }
    }

    @Override
    public void save(Block b) {
        try {
            String line = Base64.getEncoder().encodeToString(encode(b)) + System.lineSeparator();
            byte[] data = line.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buf = ByteBuffer.wrap(data);
            while (buf.hasRemaining()) logChannel.write(buf);
            logChannel.force(true);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write block log", e);
        }
        db.save(b);
    }

    @Override
    public Block findByHash(String hash) {
        return db.findByHash(hash);
    }

    @Override
    public Iterable<Block> loadAll() {
        List<Block> blocks = new ArrayList<>();
        if (!Files.exists(logPath)) return blocks;
        try (BufferedReader br = Files.newBufferedReader(logPath)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    byte[] bytes = Base64.getDecoder().decode(line.trim());
                    blocks.add(decode(bytes));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read block log", e);
        }
        return blocks;
    }

    private byte[] encode(Block b) {
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
             java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos)) {
            oos.writeObject(b);
            oos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize Block", e);
        }
    }

    private Block decode(byte[] bytes) {
        try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
             java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais)) {
            return (Block) ois.readObject();
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize Block", e);
        }
    }

    @PreDestroy
    @Override
    public void close() throws IOException {
        logChannel.close();
        db.close();
    }
}
