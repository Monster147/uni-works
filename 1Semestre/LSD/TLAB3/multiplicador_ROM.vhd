library ieee;
use ieee.std_logic_1164.all;

entity multiplicador_ROM is 
port(
		address: in std_logic_vector(4 downto 0);
		data: out std_logic_vector(9 downto 0)
		);
end multiplicador_ROM;

architecture LogicFunction of multiplicador_ROM is
begin

data <= "0001110010" when address = "00000" else
        "0001110010" when address = "00001" else
		  "0001110010" when address = "00010" else
		  "0001110010" when address = "00011" else
		  "0101110010" when address = "00100" else
		  "0101110010" when address = "00101" else
		  "0101110010" when address = "00110" else
		  "0101110010" when address = "00111" else
		  
		  "1001000011" when address = "01000" else
		  "1001000011" when address = "01010" else
		  "1001000011" when address = "01100" else
		  "1001000011" when address = "01110" else
		  "1001001011" when address = "01001" else
		  "1001001011" when address = "01011" else
		  "1001001011" when address = "01101" else
		  "1001001011" when address = "01111" else
		  
		  "0100011110" when address = "10000" else
		  "0100011110" when address = "10001" else
		  "0100011110" when address = "10100" else
		  "0100011110" when address = "10101" else
		  "1100011110" when address = "10010" else
		  "1100011110" when address = "10011" else
		  "1100011110" when address = "10110" else
		  "1100011110" when address = "10111" else
		  
		  "0010000000" when address = "11000" else
		  "0010000000" when address = "11001" else
		  "0010000000" when address = "11010" else
		  "0010000000" when address = "11011" else
		  "1110000000" when address = "11100" else
		  "1110000000" when address = "11101" else
		  "1110000000" when address = "11110" else
		  "1110000000" when address = "11111" else
		  "0001110010";
end LogicFunction;