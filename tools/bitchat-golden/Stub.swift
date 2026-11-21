// Stands in for bitchat's BitLogger, which the copied encoder files reference.
enum SecureLogger { enum Category { case security, session }
  static func debug(_ s: @autoclosure () -> String, category: Category = .session) {} }
