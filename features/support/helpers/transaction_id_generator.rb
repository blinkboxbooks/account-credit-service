module KnowsAboutTransactionIdGeneration
  attr_accessor :last_request_id

  def new_request_id
    @last_request_id = 'test-id-' + SecureRandom.uuid
  end
end
World(KnowsAboutTransactionIdGeneration)
