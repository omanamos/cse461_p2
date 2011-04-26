#!/usr/bin/ruby -w

system "javac *java"

def parse_stdout(output)
    #
    #Results for 5 trials, 5 tags per trial, 1.0% channel error rate:
    #---------------------------------------------------------
    #Average tags found:  5.0
    #Average tags missed: 0.0
    #Average runtime: 1414392.0 nanoseconds
    #Average bytes sent and received through channel: 177.8
    #Max tags found:  5
    #Max tags missed: 0
    #Max runtime: 1692262 nanoseconds
    #Min runtime: 1219689 nanoseconds
    #Max bytes single run: 260
    #Min bytes single run: 125
    
    begin
    output[3..-1].map { |line| line.split(':')[1].strip.split(' ')[0] }
    rescue Exception
        puts output
    end
end

file_prefix = ARGV.shift

bytes_output = File.open("#{file_prefix}_bytes.dat", "w")
missed_output = File.open("#{file_prefix}_missed.dat", "w")

1.upto(256/4) do |num_tags|
   num_tags *= 4
   $stderr.print "."
   stdout = `java RFIDSim 10 #{num_tags} 5`.split("\n")
   found, missed, runtime, bytes, max_found, max_missed, max_runtime, min_runtime, max_byte_run, min_byte_run = parse_stdout(stdout)
   bytes_output.puts "#{num_tags} #{bytes}"
   missed_output.puts "#{num_tags} #{missed}"
end

bytes_output.close
missed_output.close
