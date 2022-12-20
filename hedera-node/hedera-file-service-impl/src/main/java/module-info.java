module com.hedera.node.app.service.file.impl {
    requires com.hedera.node.app.service.file;
    requires com.hedera.hashgraph.protobuf.java.api;
    requires static com.github.spotbugs.annotations;

    provides com.hedera.node.app.service.file.FileService with
            com.hedera.node.app.service.file.impl.StandardFileService;

    exports com.hedera.node.app.service.file.impl to
            com.hedera.node.app.service.file.impl.test;
    exports com.hedera.node.app.service.file.impl.handlers;
}
