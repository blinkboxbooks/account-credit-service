module KnowsAboutTransactionIdGeneration
  @last_request_id

  def request_id
    @last_request_id = 'test-id-' + SecureRandom.uuid
  end

  def last_request_id
    @last_request_id
  end
end
World(KnowsAboutTransactionIdGeneration)
