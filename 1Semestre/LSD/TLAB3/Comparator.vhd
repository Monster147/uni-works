library ieee;
use ieee.std_logic_1164.all;

entity Comparator is
	port(
	A : in std_logic_vector(3 downto 0);
	B : in std_logic_vector(3 downto 0);
	R : out std_logic
	);
end Comparator;
 
architecture Comparator_ARCH of Comparator is
signal AB0, AB1, AB2, AB3:std_logic;
begin
AB3 <= A(3) xnor B(3);
AB2 <= A(2) xnor B(2);
AB1 <= A(1) xnor B(1);
AB0 <= A(0) xnor B(0);
R <= AB3 and AB2 and AB1 and AB0;
end Comparator_ARCH;