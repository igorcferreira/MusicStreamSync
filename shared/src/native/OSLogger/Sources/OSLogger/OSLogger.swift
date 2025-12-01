// The Swift Programming Language
// https://docs.swift.org/swift-book
import Foundation
import OSLog

@objc
public class OSLogger: NSObject {

    private let logger: Logger

    @objc
    public init(subsystem: String, category: String) {
        self.logger = Logger(subsystem: subsystem, category: category)
        super.init()
    }

    @objc
    public func info(message: String) {
        self.logger.info("\(message)")
    }

    @objc
    public func debug(message: String) {
        self.logger.debug("\(message)")
    }

    @objc
    public func error(message: String) {
        self.logger.error("\(message)")
    }
}
