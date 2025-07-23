package de.flashyotter.blockchain_node.grpc;

/**
 * Test stub for Tx
 */
public class Tx {
    private final double maxFee;
    private final double tip;

    private Tx(double maxFee, double tip) {
        this.maxFee = maxFee;
        this.tip = tip;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public double getMaxFee() {
        return maxFee;
    }

    public double getTip() {
        return tip;
    }

    public static class Builder {
        private double maxFee = 0.0;
        private double tip = 0.0;

        public Builder setMaxFee(double maxFee) {
            this.maxFee = maxFee;
            return this;
        }

        public Builder setTip(double tip) {
            this.tip = tip;
            return this;
        }

        public Tx build() {
            return new Tx(maxFee, tip);
        }
    }
}
