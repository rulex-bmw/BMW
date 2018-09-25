// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Data.proto

package com.eroc.bmw.pojo;

/**
 * Persistent data bean
 */
public final class DataBean {
    private DataBean() {
    }

    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistryLite registry) {
    }

    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistry registry) {
        registerAllExtensions(
                (com.google.protobuf.ExtensionRegistryLite) registry);
    }

    public interface DataOrBuilder extends
            // @@protoc_insertion_point(interface_extends:com.eroc.bmw.pojo.Data)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>required bytes param = 1;</code>
         */
        boolean hasParam();

        /**
         * <code>required bytes param = 1;</code>
         */
        com.google.protobuf.ByteString getParam();

        /**
         * <code>required bytes ts = 2;</code>
         */
        boolean hasTs();

        /**
         * <code>required bytes ts = 2;</code>
         */
        com.google.protobuf.ByteString getTs();

        /**
         * <code>optional bytes prev_hash = 3;</code>
         */
        boolean hasPrevHash();

        /**
         * <code>optional bytes prev_hash = 3;</code>
         */
        com.google.protobuf.ByteString getPrevHash();
    }

    /**
     * Protobuf type {@code com.eroc.bmw.pojo.Data}
     */
    public static final class Data extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:com.eroc.bmw.pojo.Data)
            DataOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use Data.newBuilder() to construct.
        private Data(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private Data() {
            param_ = com.google.protobuf.ByteString.EMPTY;
            ts_ = com.google.protobuf.ByteString.EMPTY;
            prevHash_ = com.google.protobuf.ByteString.EMPTY;
        }

        @Override
        public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
            return this.unknownFields;
        }

        private Data(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            this();
            if (extensionRegistry == null) {
                throw new NullPointerException();
            }
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                    com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        case 10: {
                            bitField0_ |= 0x00000001;
                            param_ = input.readBytes();
                            break;
                        }
                        case 18: {
                            bitField0_ |= 0x00000002;
                            ts_ = input.readBytes();
                            break;
                        }
                        case 26: {
                            bitField0_ |= 0x00000004;
                            prevHash_ = input.readBytes();
                            break;
                        }
                        default: {
                            if (!parseUnknownField(
                                    input, unknownFields, extensionRegistry, tag)) {
                                done = true;
                            }
                            break;
                        }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(
                        e).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
            return DataBean.internal_static_com_eroc_bmw_pojo_Data_descriptor;
        }

        @Override
        protected FieldAccessorTable
        internalGetFieldAccessorTable() {
            return DataBean.internal_static_com_eroc_bmw_pojo_Data_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            Data.class, Builder.class);
        }

        private int bitField0_;
        public static final int PARAM_FIELD_NUMBER = 1;
        private com.google.protobuf.ByteString param_;

        /**
         * <code>required bytes param = 1;</code>
         */
        public boolean hasParam() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        /**
         * <code>required bytes param = 1;</code>
         */
        public com.google.protobuf.ByteString getParam() {
            return param_;
        }

        public static final int TS_FIELD_NUMBER = 2;
        private com.google.protobuf.ByteString ts_;

        /**
         * <code>required bytes ts = 2;</code>
         */
        public boolean hasTs() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        /**
         * <code>required bytes ts = 2;</code>
         */
        public com.google.protobuf.ByteString getTs() {
            return ts_;
        }

        public static final int PREV_HASH_FIELD_NUMBER = 3;
        private com.google.protobuf.ByteString prevHash_;

        /**
         * <code>optional bytes prev_hash = 3;</code>
         */
        public boolean hasPrevHash() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        /**
         * <code>optional bytes prev_hash = 3;</code>
         */
        public com.google.protobuf.ByteString getPrevHash() {
            return prevHash_;
        }

        private byte memoizedIsInitialized = -1;

        @Override
        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized == 1) return true;
            if (isInitialized == 0) return false;

            if (!hasParam()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasTs()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(com.google.protobuf.CodedOutputStream output)
                throws java.io.IOException {
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(1, param_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, ts_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeBytes(3, prevHash_);
            }
            unknownFields.writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1) return size;

            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(1, param_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(2, ts_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(3, prevHash_);
            }
            size += unknownFields.getSerializedSize();
            memoizedSize = size;
            return size;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Data)) {
                return super.equals(obj);
            }
            Data other = (Data) obj;

            boolean result = true;
            result = result && (hasParam() == other.hasParam());
            if (hasParam()) {
                result = result && getParam()
                        .equals(other.getParam());
            }
            result = result && (hasTs() == other.hasTs());
            if (hasTs()) {
                result = result && getTs()
                        .equals(other.getTs());
            }
            result = result && (hasPrevHash() == other.hasPrevHash());
            if (hasPrevHash()) {
                result = result && getPrevHash()
                        .equals(other.getPrevHash());
            }
            result = result && unknownFields.equals(other.unknownFields);
            return result;
        }

        @Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptor().hashCode();
            if (hasParam()) {
                hash = (37 * hash) + PARAM_FIELD_NUMBER;
                hash = (53 * hash) + getParam().hashCode();
            }
            if (hasTs()) {
                hash = (37 * hash) + TS_FIELD_NUMBER;
                hash = (53 * hash) + getTs().hashCode();
            }
            if (hasPrevHash()) {
                hash = (37 * hash) + PREV_HASH_FIELD_NUMBER;
                hash = (53 * hash) + getPrevHash().hashCode();
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static Data parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Data parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Data parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Data parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Data parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Data parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Data parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static Data parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static Data parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static Data parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static Data parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static Data parseFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(Data prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE
                    ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(
                BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        /**
         * Protobuf type {@code com.eroc.bmw.pojo.Data}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:com.eroc.bmw.pojo.Data)
                DataOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
            getDescriptor() {
                return DataBean.internal_static_com_eroc_bmw_pojo_Data_descriptor;
            }

            @Override
            protected FieldAccessorTable
            internalGetFieldAccessorTable() {
                return DataBean.internal_static_com_eroc_bmw_pojo_Data_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                Data.class, Builder.class);
            }

            // Construct using com.eroc.bmw.pojo.DataBean.Data.newBuilder()
            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(
                    BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessageV3
                        .alwaysUseFieldBuilders) {
                }
            }

            @Override
            public Builder clear() {
                super.clear();
                param_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000001);
                ts_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000002);
                prevHash_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000004);
                return this;
            }

            @Override
            public com.google.protobuf.Descriptors.Descriptor
            getDescriptorForType() {
                return DataBean.internal_static_com_eroc_bmw_pojo_Data_descriptor;
            }

            @Override
            public Data getDefaultInstanceForType() {
                return Data.getDefaultInstance();
            }

            @Override
            public Data build() {
                Data result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public Data buildPartial() {
                Data result = new Data(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.param_ = param_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.ts_ = ts_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.prevHash_ = prevHash_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            @Override
            public Builder clone() {
                return (Builder) super.clone();
            }

            @Override
            public Builder setField(
                    com.google.protobuf.Descriptors.FieldDescriptor field,
                    Object value) {
                return (Builder) super.setField(field, value);
            }

            @Override
            public Builder clearField(
                    com.google.protobuf.Descriptors.FieldDescriptor field) {
                return (Builder) super.clearField(field);
            }

            @Override
            public Builder clearOneof(
                    com.google.protobuf.Descriptors.OneofDescriptor oneof) {
                return (Builder) super.clearOneof(oneof);
            }

            @Override
            public Builder setRepeatedField(
                    com.google.protobuf.Descriptors.FieldDescriptor field,
                    int index, Object value) {
                return (Builder) super.setRepeatedField(field, index, value);
            }

            @Override
            public Builder addRepeatedField(
                    com.google.protobuf.Descriptors.FieldDescriptor field,
                    Object value) {
                return (Builder) super.addRepeatedField(field, value);
            }

            @Override
            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof Data) {
                    return mergeFrom((Data) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(Data other) {
                if (other == Data.getDefaultInstance()) return this;
                if (other.hasParam()) {
                    setParam(other.getParam());
                }
                if (other.hasTs()) {
                    setTs(other.getTs());
                }
                if (other.hasPrevHash()) {
                    setPrevHash(other.getPrevHash());
                }
                this.mergeUnknownFields(other.unknownFields);
                onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                if (!hasParam()) {
                    return false;
                }
                if (!hasTs()) {
                    return false;
                }
                return true;
            }

            @Override
            public Builder mergeFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws java.io.IOException {
                Data parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (Data) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private com.google.protobuf.ByteString param_ = com.google.protobuf.ByteString.EMPTY;

            /**
             * <code>required bytes param = 1;</code>
             */
            public boolean hasParam() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            /**
             * <code>required bytes param = 1;</code>
             */
            public com.google.protobuf.ByteString getParam() {
                return param_;
            }

            /**
             * <code>required bytes param = 1;</code>
             */
            public Builder setParam(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                param_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>required bytes param = 1;</code>
             */
            public Builder clearParam() {
                bitField0_ = (bitField0_ & ~0x00000001);
                param_ = getDefaultInstance().getParam();
                onChanged();
                return this;
            }

            private com.google.protobuf.ByteString ts_ = com.google.protobuf.ByteString.EMPTY;

            /**
             * <code>required bytes ts = 2;</code>
             */
            public boolean hasTs() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            /**
             * <code>required bytes ts = 2;</code>
             */
            public com.google.protobuf.ByteString getTs() {
                return ts_;
            }

            /**
             * <code>required bytes ts = 2;</code>
             */
            public Builder setTs(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                ts_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>required bytes ts = 2;</code>
             */
            public Builder clearTs() {
                bitField0_ = (bitField0_ & ~0x00000002);
                ts_ = getDefaultInstance().getTs();
                onChanged();
                return this;
            }

            private com.google.protobuf.ByteString prevHash_ = com.google.protobuf.ByteString.EMPTY;

            /**
             * <code>optional bytes prev_hash = 3;</code>
             */
            public boolean hasPrevHash() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            /**
             * <code>optional bytes prev_hash = 3;</code>
             */
            public com.google.protobuf.ByteString getPrevHash() {
                return prevHash_;
            }

            /**
             * <code>optional bytes prev_hash = 3;</code>
             */
            public Builder setPrevHash(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000004;
                prevHash_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>optional bytes prev_hash = 3;</code>
             */
            public Builder clearPrevHash() {
                bitField0_ = (bitField0_ & ~0x00000004);
                prevHash_ = getDefaultInstance().getPrevHash();
                onChanged();
                return this;
            }

            @Override
            public final Builder setUnknownFields(
                    final com.google.protobuf.UnknownFieldSet unknownFields) {
                return super.setUnknownFields(unknownFields);
            }

            @Override
            public final Builder mergeUnknownFields(
                    final com.google.protobuf.UnknownFieldSet unknownFields) {
                return super.mergeUnknownFields(unknownFields);
            }


            // @@protoc_insertion_point(builder_scope:com.eroc.bmw.pojo.Data)
        }

        // @@protoc_insertion_point(class_scope:com.eroc.bmw.pojo.Data)
        private static final Data DEFAULT_INSTANCE;

        static {
            DEFAULT_INSTANCE = new Data();
        }

        public static Data getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        @Deprecated
        public static final com.google.protobuf.Parser<Data>
                PARSER = new com.google.protobuf.AbstractParser<Data>() {
            @Override
            public Data parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new Data(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<Data> parser() {
            return PARSER;
        }

        @Override
        public com.google.protobuf.Parser<Data> getParserForType() {
            return PARSER;
        }

        @Override
        public Data getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    private static final com.google.protobuf.Descriptors.Descriptor
            internal_static_com_eroc_bmw_pojo_Data_descriptor;
    private static final
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
            internal_static_com_eroc_bmw_pojo_Data_fieldAccessorTable;

    public static com.google.protobuf.Descriptors.FileDescriptor
    getDescriptor() {
        return descriptor;
    }

    private static com.google.protobuf.Descriptors.FileDescriptor
            descriptor;

    static {
        String[] descriptorData = {
                "\n\nData.proto\022\021com.eroc.bmw.pojo\"4\n\004Data\022" +
                        "\r\n\005param\030\001 \002(\014\022\n\n\002ts\030\002 \002(\014\022\021\n\tprev_hash\030" +
                        "\003 \001(\014B\nB\010DataBean"
        };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
                new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
                    public com.google.protobuf.ExtensionRegistry assignDescriptors(
                            com.google.protobuf.Descriptors.FileDescriptor root) {
                        descriptor = root;
                        return null;
                    }
                };
        com.google.protobuf.Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new com.google.protobuf.Descriptors.FileDescriptor[]{
                        }, assigner);
        internal_static_com_eroc_bmw_pojo_Data_descriptor =
                getDescriptor().getMessageTypes().get(0);
        internal_static_com_eroc_bmw_pojo_Data_fieldAccessorTable = new
                com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_com_eroc_bmw_pojo_Data_descriptor,
                new String[]{"Param", "Ts", "PrevHash",});
    }

    // @@protoc_insertion_point(outer_class_scope)
}